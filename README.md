# DebuggerX - MCP Fork

> Fork de [debuggerX](https://github.com/ouoou/debuggerX) avec améliorations pour l'intégration MCP/Claude Code.

## Qu'est-ce que debuggerX ?

DebuggerX est un proxy JDWP qui permet à **plusieurs debuggers de se connecter simultanément à une même JVM**. Il facilite le debugging collaboratif en permettant à plusieurs développeurs (ou outils) de partager les breakpoints et l'état de debug.

## Fonctionnalités ajoutées dans ce fork

### 1. Résolution des breakpoints

**Problème original :** Les breakpoints étaient trackés uniquement avec des IDs JDWP bruts (classId, methodId, codeIndex). Impossible de savoir où se trouve un breakpoint sans un debugger externe.

**Solution :** Le fork résout automatiquement les breakpoints en noms lisibles :
- `className` : Nom complet de la classe (ex: `com.axelor.apps.repository.DMSFileRepository`)
- `lineNumber` : Numéro de ligne dans le fichier source

**Pourquoi :** Essentiel pour qu'une IA (Claude Code) puisse comprendre l'état du debug sans avoir besoin d'un IDE.

### 2. API HTTP complète

**Nouveau endpoint ajouté :**
```bash
# Lister tous les breakpoints avec infos résolues
GET http://localhost:55006/breakpoints

# Supprimer un breakpoint spécifique
DELETE http://localhost:55006/breakpoints/{requestId}
```

**Pourquoi :** Permet le contrôle programmatique des breakpoints via MCP ou scripts, sans dépendre d'un IDE.

### 3. Découverte multi-client des breakpoints

**Fonctionnalité clé :** Le fork permet de **lister TOUS les breakpoints positionnés par TOUS les clients connectés** (IntelliJ, Eclipse, MCP, etc.).

**Exemple :**
- IntelliJ pose un breakpoint sur `MyClass:42`
- Claude Code peut voir ce breakpoint via `GET /breakpoints`
- Claude Code peut analyser le contexte et supprimer le breakpoint si besoin

**Pourquoi :** Coordination entre debuggers humains (IDE) et IA (MCP). L'IA peut voir et gérer les breakpoints existants sans interférer avec le workflow des développeurs.

### 4. Corrections de bugs critiques

- **Fix NullPointerException** dans le système de résolution (DebuggerService.java:159-172)
- **Enregistrement des packets** du BreakpointResolver dans la packetMap
- **Capture des logs** du proxy dans `debuggerx-proxy.log`

## Utilisation

### Démarrage du proxy

```bash
java -jar debuggerx-bootstrap-1.0-SNAPSHOT.jar
```

**Ports par défaut :**
- JVM JDWP : `5005`
- Proxy JDWP : `55005` (connectez votre IDE ici)
- API HTTP : `55006`

### Connexion IDE

**IntelliJ / Eclipse :**
- Host: `localhost`
- Port: `55005`

### API HTTP

```bash
# Voir tous les breakpoints (tous clients)
curl http://localhost:55006/breakpoints

# Résultat exemple :
# {
#   "breakpoints": [
#     {
#       "requestId": 17,
#       "className": "com.axelor.apps.repository.DMSFileRepository",
#       "lineNumber": 31,
#       "classId": 16681,
#       "methodId": 1877543952736,
#       "codeIndex": 0
#     }
#   ]
# }

# Supprimer un breakpoint
curl -X DELETE http://localhost:55006/breakpoints/17
```

## Intégration MCP

Ce fork est conçu pour fonctionner avec le serveur MCP JDWP qui expose 21 outils de debugging pour Claude Code :
- `jdwp_list_all_breakpoints()` - Voir tous les breakpoints (tous clients)
- `jdwp_clear_breakpoint_by_id(requestId)` - Supprimer un breakpoint
- `jdwp_get_threads()`, `jdwp_get_stack()`, `jdwp_get_locals()`, etc.

Voir [mcp-jdwp-java](https://github.com/NicolasVautrin/mcp-jdwp-java) pour le serveur MCP complet.

## Architecture

```
┌─────────────────────┐
│   Claude Code AI    │
└──────────┬──────────┘
           │ MCP
┌──────────▼──────────┐
│  MCP JDWP Server    │
└──────────┬──────────┘
           │ HTTP API (55006) + JDI (55005)
┌──────────▼──────────┐
│  debuggerX Proxy    │ ← CE FORK
│  - Résolution BP    │
│  - API HTTP         │
│  - Multi-client BP  │
└──────────┬──────────┘
           │ JDWP (5005)
┌──────────▼──────────┐
│    JVM / App Java   │
└─────────────────────┘
```

## Documentation technique

Pour les détails techniques complets sur les modifications, voir [FORK_RATIONALE.md](./FORK_RATIONALE.md).

## Crédits

- **Projet original** : [@ouoou/debuggerX](https://github.com/ouoou/debuggerX)
- **Fork** : Nicolas Vautrin & Claude Code
- **MCP** : [Anthropic Model Context Protocol](https://modelcontextprotocol.io)
