# Pourquoi ce Fork de debuggerX ?

## Contexte

Le projet [debuggerX original](https://github.com/wuou-learn/debuggerX) est un excellent proxy JDWP qui permet à plusieurs debuggers de se connecter simultanément à une JVM. Cependant, pour l'intégration avec **MCP (Model Context Protocol)** et **Claude Code**, plusieurs fonctionnalités critiques manquaient.

## Limitations du debuggerX Original

### 1. ❌ Pas de résolution des breakpoints

**Problème :**
- Les breakpoints étaient trackés uniquement avec des IDs JDWP bruts (classId, methodId, codeIndex)
- Impossible de savoir **où** se trouve un breakpoint sans debugger externe
- L'API HTTP retournait des informations illisibles pour un humain ou une IA

**Exemple de sortie originale :**
```
Breakpoint requestId=17
  classId=16681 (unresolved)
  methodId=1877543952736
  codeIndex=0
```

**Impact :**
- Claude Code ne pouvait pas comprendre où se trouvent les breakpoints
- Pas d'affichage du nom de classe ou du numéro de ligne
- Inutilisable pour une assistance IA au debugging

### 2. ❌ Pas de gestion des breakpoints via API

**Problème :**
- Aucun endpoint pour supprimer un breakpoint
- Seul IntelliJ (ou autre IDE) pouvait gérer ses propres breakpoints
- Impossible pour Claude Code de nettoyer les breakpoints après analyse

**Impact :**
- Les breakpoints s'accumulent sans moyen de les supprimer programmatiquement
- Dépendance totale à l'IDE pour la gestion

### 3. ❌ Crash lors de la résolution automatique

**Problème :**
- Le code de `BreakpointResolver` existait mais crashait avec `NullPointerException`
- Les packets internes du resolver n'étaient pas enregistrés dans le mapping
- Le système de routage ne gérait pas les packets sans origine

**Code problématique :**
```java
private void mapResponseCommand(JdwpPacket packet) {
    JdwpPacket originPacket = session.findPacketByNewId(packet.getHeader().getId());
    // ❌ NullPointerException si originPacket est null (packets du BreakpointResolver)
    packet.getHeader().setCommandSet(originPacket.getHeader().getCommandSet());
    packet.getHeader().setCommand(originPacket.getHeader().getCommand());
}
```

**Impact :**
- Les breakpoints ne pouvaient jamais être résolus
- Logs remplis d'erreurs NullPointerException
- Fonctionnalité de résolution complètement cassée

### 4. ❌ Logs du proxy inaccessibles

**Problème :**
- Le proxy loggait sur stdout/stderr mais ces logs n'étaient jamais capturés
- Impossible de debugger les problèmes de résolution de breakpoints
- Pas de visibilité sur le fonctionnement interne

**Impact :**
- Debugging du proxy lui-même très difficile
- Impossible de diagnostiquer les problèmes de routage ou de résolution

## Nos Améliorations

### 1. ✅ Résolution complète des breakpoints

**Ce qui a été ajouté :**

#### A. Fix du NullPointerException dans `DebuggerService`
```java
private void mapResponseCommand(JdwpPacket packet) {
    DebugSession session = SessionManager.getInstance().findJvmServerSession();
    JdwpPacket originPacket = session.findPacketByNewId(packet.getHeader().getId());

    // ✅ Guard contre null pour les packets internes
    if (originPacket == null) {
        log.warn("[mapResponseCommand] No origin packet found for ID {}, cannot map command",
            packet.getHeader().getId());
        return;
    }

    packet.getHeader().setCommandSet(originPacket.getHeader().getCommandSet());
    packet.getHeader().setCommand(originPacket.getHeader().getCommand());
}
```

#### B. Enregistrement des packets du BreakpointResolver
```java
// Dans BreakpointResolver.java
JdwpPacket packet = new JdwpPacket(header, buffer.array());

// Store mapping so reply processor can find the breakpoint
session.getPendingResolutions().put(packetId, breakpointInfo.getRequestId());

// ✅ NOUVEAU : Register packet in packetMap so mapResponseCommand can find it
session.getPacketMap().put(packetId, packet);

jvmChannel.writeAndFlush(packet);
```

#### C. Reply Processors fonctionnels
- `ReferenceTypeSignatureReplyProcessor` : Résout classId → className
- `MethodLineTableReplyProcessor` : Résout codeIndex → lineNumber
- Mise à jour automatique de `BreakpointInfo` avec les informations résolues

**Résultat :**
```
Breakpoint 1 (Request ID: 17):
  Class: com.axelor.apps.openauctionbase.repository.AuctionHeaderRepositoryExt
  Line: 31
  Code Index: 0
```

### 2. ✅ API HTTP complète pour la gestion

**Endpoints ajoutés :**

#### DELETE /breakpoints/{requestId}
Supprime un breakpoint spécifique en envoyant `EventRequest.Clear` à la JVM.

**Implémentation :**
```java
private boolean clearBreakpoint(DebugSession session, int requestId) {
    int packetId = packetIdGenerator.incrementAndGet();

    // Build JDWP packet: EventRequest.Clear (CommandSet=15, Command=2)
    ByteBuffer buffer = ByteBuffer.allocate(5);
    buffer.put((byte) 2); // BREAKPOINT event kind
    buffer.putInt(requestId);

    JdwpHeader header = new JdwpHeader();
    header.setId(packetId);
    header.setFlags(JdwpConstants.FLAG_COMMAND);
    header.setCommandSet((byte) 15); // EventRequest
    header.setCommand((byte) 2); // Clear

    JdwpPacket packet = new JdwpPacket(header, buffer.array());
    session.getPacketMap().put(packetId, packet);

    jvmChannel.writeAndFlush(packet);
    session.getGlobalBreakpoints().remove(requestId);

    return true;
}
```

**Utilisation :**
```bash
curl -X DELETE http://localhost:55006/breakpoints/17
# {"message": "Breakpoint 17 cleared successfully"}
```

### 3. ✅ Intégration MCP avec parsing JSON robuste

**Problème original :**
Le serveur MCP parsait manuellement le JSON avec des regex fragiles (~150 lignes de code).

**Notre solution :**
Utilisation de **Jackson** (déjà disponible dans Spring Boot) :

```java
// Parse JSON using Jackson
ObjectMapper mapper = new ObjectMapper();
JsonNode root = mapper.readTree(body);
JsonNode breakpointsArray = root.get("breakpoints");

for (JsonNode bp : breakpointsArray) {
    int requestId = bp.get("requestId").asInt();
    String className = bp.get("className").asText();
    int lineNumber = bp.get("lineNumber").asInt();

    // Affichage propre et fiable
    result.append(String.format("  Class: %s\n", className));
    result.append(String.format("  Line: %d\n", lineNumber));
}
```

**Bénéfices :**
- Code réduit de ~150 lignes
- Plus robuste (gère null, types, etc.)
- Plus maintenable

### 4. ✅ MCP Tools pour Claude Code

**21 outils JDWP ajoutés :**

#### Connexion & Info
- `jdwp_connect()` - Connexion automatique avec démarrage du proxy
- `jdwp_disconnect()` - Déconnexion
- `jdwp_get_version()` - Info JVM

#### Navigation
- `jdwp_get_threads()` - Liste des threads
- `jdwp_get_stack(threadId)` - Stack trace
- `jdwp_get_locals(threadId, frameIndex)` - Variables locales
- `jdwp_get_fields(objectId)` - Champs d'objets
- `jdwp_invoke_method(threadId, objectId, methodName)` - Exécuter méthodes

#### Contrôle d'exécution
- `jdwp_resume()` / `jdwp_resume_thread(threadId)` - Reprendre
- `jdwp_suspend_thread(threadId)` - Suspendre
- `jdwp_step_over(threadId)` - Step over
- `jdwp_step_into(threadId)` - Step into
- `jdwp_step_out(threadId)` - Step out

#### Gestion des breakpoints
- `jdwp_set_breakpoint(className, lineNumber)` - Créer breakpoint
- `jdwp_clear_breakpoint(className, lineNumber)` - Supprimer breakpoint
- `jdwp_list_breakpoints()` - Lister breakpoints JDI
- `jdwp_list_all_breakpoints()` - **Lister TOUS les breakpoints (tous clients)**
- `jdwp_clear_breakpoint_by_id(requestId)` - **Supprimer par ID**
- `jdwp_clear_all_breakpoints()` - Supprimer tous

#### Événements
- `jdwp_get_events(count)` - Historique des événements JDWP
- `jdwp_clear_events()` - Nettoyer l'historique
- `jdwp_configure_exception_monitoring(...)` - Config exceptions
- `jdwp_get_exception_config()` - Voir config exceptions

### 5. ✅ Logs du proxy capturés

**Configuration dans `DebuggerXManager.java` :**
```java
File logFile = new File(workingDir, "debuggerx-proxy.log");
pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
pb.redirectError(ProcessBuilder.Redirect.appendTo(logFile));
System.err.println("[INFO] Proxy logs will be written to: " + logFile.getAbsolutePath());
```

**Résultat :**
- Logs visibles dans `debuggerx-proxy.log`
- Possibilité de debugger les problèmes de résolution
- Traçabilité complète du fonctionnement

## Cas d'Usage Activés par le Fork

### 1. Debugging Assisté par IA

**Scénario :**
```
User: "Claude, analyse la requête HTTP qui plante au breakpoint"

Claude:
1. jdwp_list_all_breakpoints() → Voit les breakpoints IntelliJ
2. jdwp_get_threads() → Trouve le thread HTTP suspendu
3. jdwp_get_stack(threadId) → Analyse la stack trace
4. jdwp_get_locals(threadId, 0) → Examine les variables
5. jdwp_get_fields(requestId) → Inspecte l'objet request
6. jdwp_invoke_method(threadId, requestId, "getBody") → Lit le body
7. jdwp_resume_thread(threadId) → Reprend l'exécution
```

**Impossible sans le fork :**
- Claude ne verrait que des IDs (classId=16681)
- Pas de suppression des breakpoints après analyse
- Pas d'accès aux breakpoints IntelliJ

### 2. Multi-Debugger Synchronisé

**Scénario :**
- IntelliJ pose des breakpoints pendant le dev
- Claude analyse les breakpoints existants via `jdwp_list_all_breakpoints()`
- Les deux voient les mêmes breakpoints résolus (className + lineNumber)
- Claude peut supprimer les breakpoints temporaires via `jdwp_clear_breakpoint_by_id()`

### 3. Automation de Tests

**Scénario :**
```python
# Script de test automatisé via MCP
1. Set breakpoint à la ligne critique
2. Déclencher requête HTTP
3. Attendre suspension au breakpoint
4. Vérifier l'état des variables
5. Supprimer le breakpoint
6. Reprendre l'exécution
```

## Comparaison Avant/Après

| Fonctionnalité | debuggerX Original | Notre Fork |
|----------------|-------------------|------------|
| Multi-debugger | ✅ Fonctionne | ✅ Fonctionne |
| Résolution breakpoints | ❌ Crashe (NPE) | ✅ Fonctionne |
| API HTTP GET /breakpoints | ✅ IDs bruts | ✅ Infos résolues |
| API HTTP DELETE /breakpoints | ❌ Absent | ✅ Ajouté |
| Logs proxy | ❌ Perdus | ✅ Capturés |
| Intégration MCP | ❌ Impossible | ✅ 21 tools |
| Parsing JSON | N/A | ✅ Jackson |
| Support IA | ❌ Non | ✅ Claude Code |

## Architecture du Fork

```
┌─────────────────────┐
│   Claude Code AI    │ ← Nouveau : Assistance IA
└──────────┬──────────┘
           │ MCP Protocol
┌──────────▼──────────┐
│  MCP JDWP Server    │ ← Nouveau : 21 outils JDWP
│  - Jackson parsing  │
│  - JDI integration  │
└──────────┬──────────┘
           │ HTTP API (55006)
           │ JDI (55005)
┌──────────▼──────────┐
│  debuggerX Proxy    │ ← Fork avec améliorations
│  - Breakpoint res.  │ ✅ Fix NPE
│  - DELETE endpoint  │ ✅ Nouveau
│  - Logs captured    │ ✅ Nouveau
└──────────┬──────────┘
           │ JDWP (61959)
┌──────────▼──────────┐
│  Java App / JVM     │
└─────────────────────┘
```

## Conclusion

Le fork était **essentiel** pour :

1. **Corriger les bugs critiques** (NullPointerException dans la résolution)
2. **Ajouter la gestion programmatique** des breakpoints (DELETE endpoint)
3. **Rendre l'information accessible** à une IA (résolution + parsing JSON robuste)
4. **Permettre l'assistance IA** au debugging via MCP

Sans ces modifications, debuggerX restait un excellent outil pour les **humains avec IDE**, mais **inutilisable pour l'IA** et l'automation.

Notre fork transforme debuggerX en une **plateforme de debugging programmable** tout en conservant 100% de compatibilité avec l'usage original.

## Contribution Potentielle Upstream

Certaines améliorations pourraient être contribuées au projet original :
- ✅ Fix du NullPointerException dans `DebuggerService`
- ✅ Enregistrement des packets du `BreakpointResolver`
- ✅ Endpoint DELETE pour la gestion des breakpoints
- ✅ Capture des logs du proxy

Les parties spécifiques à MCP (serveur Spring Boot, 21 tools) resteraient dans notre fork car elles sont spécifiques à l'intégration Claude Code.
