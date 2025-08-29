package welbre.ambercraft.sim;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VersionHandler {
     List<VersionConversor> versionList;

    private VersionHandler(List<VersionConversor> versionList) {
        this.versionList = versionList;
    }


    public static Builder builder(){
        return new Builder();
    }

    public void handleRead(CompoundTag tag) {
        final short version = tag.contains("version") ? tag.getShort("version") : 0;

        if (version > versionList.size())
            throw new IllegalArgumentException("Version %d is invalid!".formatted(version));

        for (int i = version; i < versionList.size(); i++)
        {
            VersionConversor versionConversor = versionList.get(i);
            versionConversor.upConverted.accept(tag);
        }
    }

    public void handleWrite(CompoundTag tag) {
        tag.putShort("version", (short) versionList.size());
    }


    public static final class Builder {
        List<VersionConversor> versionList = new ArrayList<>();

        private Builder() {
        }

        public VersionConversor.Builder initVersion(String name, boolean breakingVersion)
        {
            return new VersionConversor.Builder(this, (short) versionList.size(), name, breakingVersion);
        }

        public VersionConversor.Builder initVersion()
        {
            return new VersionConversor.Builder(this, (short) versionList.size(), "v" + versionList.size(), false);
        }

        public VersionHandler build()
        {
            return new VersionHandler(versionList);
        }
    }


    public record VersionConversor(short version, String name, boolean breakingVersion, Consumer<CompoundTag> upConverted, Consumer<CompoundTag> downConverted)
    {
        public static final class Builder
        {
            private final VersionHandler.Builder vhb;
            private final short version;
            private final String name;
            private final boolean breaking;
            private Consumer<CompoundTag> upConverter;
            private Consumer<CompoundTag> downConverter;

            private Builder(VersionHandler.Builder builder, short version, String name, boolean brakingVersion) {
                this.vhb = builder;
                this.version = version;
                this.name = name;
                this.breaking = brakingVersion;
            }

            public Builder upConvert(Consumer<CompoundTag> converter){
                this.upConverter = converter;
                return this;
            }

            public Builder downConvert(Consumer<CompoundTag> converter){
                this.downConverter = converter;
                return this;
            }

            public VersionHandler.Builder build(){
                vhb.versionList.add(new VersionConversor(version, name, breaking, upConverter, downConverter));
                return vhb;
            }
        }
    }

}
