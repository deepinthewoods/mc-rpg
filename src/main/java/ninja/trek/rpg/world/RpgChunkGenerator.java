package ninja.trek.rpg.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RpgChunkGenerator extends ChunkGenerator {

    public static final MapCodec<RpgChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Biome.CODEC.fieldOf("biome").forGetter(gen -> gen.biome)
        ).apply(instance, RpgChunkGenerator::new)
    );

    private static final int BASE_HEIGHT = 64;
    private static final double AMPLITUDE = 10.0;
    private static final double FREQUENCY = 0.005;

    private final Holder<Biome> biome;

    public RpgChunkGenerator(Holder<Biome> biome) {
        super(new FixedBiomeSource(biome));
        this.biome = biome;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int chunkX = chunk.getPos().getMinBlockX();
        int chunkZ = chunk.getPos().getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX + x;
                int worldZ = chunkZ + z;
                int height = getHeightAt(worldX, worldZ);

                // Grass on top
                mutable.set(worldX, height, worldZ);
                chunk.setBlockState(mutable, Blocks.GRASS_BLOCK.defaultBlockState(), 0);

                // 3 layers of dirt
                for (int y = height - 1; y >= height - 3 && y >= chunk.getMinY(); y--) {
                    mutable.set(worldX, y, worldZ);
                    chunk.setBlockState(mutable, Blocks.DIRT.defaultBlockState(), 0);
                }

                // Stone down to bedrock + 1
                for (int y = height - 4; y >= chunk.getMinY() + 1; y--) {
                    mutable.set(worldX, y, worldZ);
                    chunk.setBlockState(mutable, Blocks.STONE.defaultBlockState(), 0);
                }

                // Bedrock at bottom
                mutable.set(worldX, chunk.getMinY(), worldZ);
                chunk.setBlockState(mutable, Blocks.BEDROCK.defaultBlockState(), 0);
            }
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        // Surface is built in buildSurface; just fill stone base here
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int chunkX = chunk.getPos().getMinBlockX();
        int chunkZ = chunk.getPos().getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX + x;
                int worldZ = chunkZ + z;
                int height = getHeightAt(worldX, worldZ);

                for (int y = chunk.getMinY(); y <= height; y++) {
                    mutable.set(worldX, y, worldZ);
                    chunk.setBlockState(mutable, Blocks.STONE.defaultBlockState(), 0);
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        return getHeightAt(x, z) + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        int height = getHeightAt(x, z);
        int minY = level.getMinY();
        int totalHeight = level.getHeight();
        BlockState[] states = new BlockState[totalHeight];

        for (int i = 0; i < totalHeight; i++) {
            int y = minY + i;
            if (y == minY) {
                states[i] = Blocks.BEDROCK.defaultBlockState();
            } else if (y <= height - 4) {
                states[i] = Blocks.STONE.defaultBlockState();
            } else if (y <= height - 1) {
                states[i] = Blocks.DIRT.defaultBlockState();
            } else if (y == height) {
                states[i] = Blocks.GRASS_BLOCK.defaultBlockState();
            } else {
                states[i] = Blocks.AIR.defaultBlockState();
            }
        }

        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("RPG Terrain Generator");
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk) {
        // No caves
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        // No mob spawning
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return -64; // No sea level (no water)
    }

    private int getHeightAt(int x, int z) {
        // Simple 2D simplex-like noise using sine combinations for gentle rolling hills
        double nx = x * FREQUENCY;
        double nz = z * FREQUENCY;
        double noise = Math.sin(nx * 1.0 + 0.3) * Math.cos(nz * 0.8 + 0.7)
            + 0.5 * Math.sin(nx * 2.1 + nz * 1.3 + 1.2)
            + 0.25 * Math.sin(nx * 4.3 - nz * 3.7 + 2.5);
        // Normalize to roughly [-1, 1]
        noise /= 1.75;
        return BASE_HEIGHT + (int)(noise * AMPLITUDE);
    }
}
