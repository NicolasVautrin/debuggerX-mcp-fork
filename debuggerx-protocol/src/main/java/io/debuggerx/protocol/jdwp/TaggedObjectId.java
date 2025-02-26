package io.debuggerx.protocol.jdwp;


import java.nio.ByteBuffer;

/**
 * @author ouwu
 */
public class TaggedObjectId {

    private final byte tag;
    private final ObjectId objectId;

    public static TaggedObjectId read(ByteBuffer byteBuffer, IdSizes idSizes) {
        return new TaggedObjectId(byteBuffer, idSizes);
    }

    TaggedObjectId(ByteBuffer byteBuffer, IdSizes idSizes) {
        tag = byteBuffer.get();
        objectId = new ObjectId(byteBuffer, idSizes);
    }

    public TaggedObjectId(byte tag, ObjectId objectId) {
        this.tag = tag;
        this.objectId = objectId;
    }

    public byte getTag() {
        return tag;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public long asLong() {
        return getObjectId().asLong();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TaggedObjectId that = (TaggedObjectId) o;

        if (tag != that.tag) {
            return false;
        }
        return objectId != null ? objectId.equals(that.objectId) : that.objectId == null;
    }

    @Override
    public int hashCode() {
        int result = (int) tag;
        result = 31 * result + (objectId != null ? objectId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TaggedObjectId{" +
                "tag=" + tag +
                ", objectId=" + asLong() +
                '}';
    }
}
