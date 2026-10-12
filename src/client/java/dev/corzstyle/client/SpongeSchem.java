package dev.corzstyle.client;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** Safe, dependency-free Sponge .schem V2/V3 reader. No block entities/entities are executed. */
public final class SpongeSchem {
    public record Block(int x, int y, int z, String state) {
        public String blockId() {
            int properties = state.indexOf('[');
            return properties >= 0 ? state.substring(0, properties) : state;
        }
    }
    public record Schematic(int width, int height, int length, List<Block> blocks) { }
    private static final int MAX_BYTES = 16 * 1024 * 1024;
    private static final int MAX_VOLUME = 200_000;

    public static Schematic read(Path path) throws IOException {
        if (!path.toString().endsWith(".schem")) throw new IOException("Expected .schem (Sponge v2/v3)");
        if (Files.size(path) > MAX_BYTES) throw new IOException("File exceeds 16 MB limit");
        final Map<String,Object> root;
        try (var in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(path))))) {
            if (in.readUnsignedByte() != 10) throw new IOException("Expected root NBT compound");
            in.readUTF();
            root = readCompound(in, 0);
        }
        Map<String,Object> schem = asMap(root.getOrDefault("Schematic", root));
        int w = number(schem.get("Width")), h = number(schem.get("Height")), l = number(schem.get("Length"));
        long volume = (long) w*h*l;
        if (w <= 0 || h <= 0 || l <= 0 || volume > MAX_VOLUME) throw new IOException("Invalid schematic size / exceeds 200,000 blocks");
        int version = number(schem.get("Version"));
        Map<String,Object> section = version >= 3 ? asMap(schem.get("Blocks")) : schem;
        Map<String,Object> palette = asMap(section.get("Palette"));
        byte[] raw = (byte[]) section.get(version >= 3 ? "Data" : "BlockData");
        if (raw == null || raw.length > MAX_VOLUME*5) throw new IOException("Missing or oversized block data");
        Map<Integer,String> lookup = new HashMap<>();
        for (var entry : palette.entrySet()) lookup.put(number(entry.getValue()), entry.getKey());
        List<Block> blocks = new ArrayList<>();
        int index = 0, offset = 0;
        for (int y=0; y<h; y++) for (int z=0; z<l; z++) for (int x=0; x<w; x++) {
            int value=0, shift=0, next;
            do {
                if (offset >= raw.length || shift > 28) throw new IOException("Corrupt varint block data");
                next=raw[offset++] & 255;
                value |= (next & 127) << shift;
                shift += 7;
            } while ((next & 128) != 0);
            String id = lookup.get(value);
            if (id == null) throw new IOException("Missing block palette id " + value);
            if (!id.equals("minecraft:air") && !id.equals("minecraft:cave_air") && !id.equals("minecraft:void_air")) {
                blocks.add(new Block(x,y,z,id));
            }
            index++;
        }
        return new Schematic(w, h, l, Collections.unmodifiableList(blocks));
    }

    private static Map<String,Object> asMap(Object value) throws IOException {
        if (!(value instanceof Map<?,?> map)) throw new IOException("Invalid schematic NBT compound");
        @SuppressWarnings("unchecked") Map<String,Object> cast = (Map<String,Object>)map;
        return cast;
    }
    private static int number(Object value) throws IOException {
        if (!(value instanceof Number n)) throw new IOException("Invalid schematic NBT numeric field");
        return n.intValue();
    }
    private static Map<String,Object> readCompound(DataInput in, int depth) throws IOException {
        if (depth>32) throw new IOException("Too deeply nested NBT");
        Map<String,Object> result=new HashMap<>();
        for (int count=0; count < 100000; count++) {
            int type = in.readUnsignedByte();
            if (type == 0) return result;
            String name=in.readUTF();
            if (name.length()>16384) throw new IOException("NBT name too long");
            Object val=readTag(in,type,depth+1);
            if (val != null) result.put(name,val);
        }
        throw new IOException("Too many NBT fields");
    }
    private static Object readTag(DataInput in, int type, int depth) throws IOException {
        if (depth > 32) throw new IOException("NBT nesting too deep");
        return switch(type) {
            case 1 -> in.readByte();
            case 2 -> in.readShort();
            case 3 -> in.readInt();
            case 4 -> in.readLong();
            case 5 -> in.readFloat();
            case 6 -> in.readDouble();
            case 7 -> { int n=in.readInt(); checked(n,MAX_BYTES); byte[] bytes=new byte[n]; in.readFully(bytes); yield bytes; }
            case 8 -> in.readUTF();
            case 9 -> { int child=in.readUnsignedByte(), n=in.readInt(); checked(n,MAX_VOLUME); for(int i=0;i<n;i++) readTag(in,child,depth+1); yield null; }
            case 10 -> readCompound(in,depth+1);
            case 11 -> { int n=in.readInt(); checked(n,MAX_VOLUME); for(int i=0;i<n;i++) in.readInt(); yield null; }
            case 12 -> { int n=in.readInt(); checked(n,MAX_VOLUME); for(int i=0;i<n;i++) in.readLong(); yield null; }
            default -> throw new IOException("Unknown NBT tag: " + type);
        };
    }
    private static void checked(int n,int max) throws IOException {
        if (n<0 || n>max) throw new IOException("Invalid NBT length " + n);
    }
    private SpongeSchem() {}
}
