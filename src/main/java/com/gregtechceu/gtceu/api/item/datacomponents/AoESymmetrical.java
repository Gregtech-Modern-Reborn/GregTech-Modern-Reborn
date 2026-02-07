package com.gregtechceu.gtceu.api.item.datacomponents;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.Accessors;

public record AoESymmetrical(int maxColumn, int maxRow, int maxLayer, int column, int row, int layer) {

    public static final Codec<AoESymmetrical> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_column").forGetter(AoESymmetrical::maxColumn),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_row").forGetter(AoESymmetrical::maxRow),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_layer").forGetter(AoESymmetrical::maxLayer),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("column").forGetter(AoESymmetrical::column),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("row").forGetter(AoESymmetrical::row),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("layer").forGetter(AoESymmetrical::layer))
            .apply(instance, AoESymmetrical::new));
    public static final StreamCodec<ByteBuf, AoESymmetrical> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AoESymmetrical::maxColumn,
            ByteBufCodecs.VAR_INT, AoESymmetrical::maxRow,
            ByteBufCodecs.VAR_INT, AoESymmetrical::maxLayer,
            ByteBufCodecs.VAR_INT, AoESymmetrical::column,
            ByteBufCodecs.VAR_INT, AoESymmetrical::row,
            ByteBufCodecs.VAR_INT, AoESymmetrical::layer,
            AoESymmetrical::new);

    public static final AoESymmetrical ZERO = new AoESymmetrical(0, 0, 0, 0, 0, 0);

    public boolean isZero() {
        return this == ZERO || (this.maxColumn == 0 && this.maxRow == 0 && this.maxLayer == 0);
    }

    public static AoESymmetrical of(int column, int row, int layer) {
        Preconditions.checkArgument(column >= 0, "Height cannot be negative.");
        Preconditions.checkArgument(row >= 0, "Width cannot be negative.");
        Preconditions.checkArgument(layer >= 0, "Depth cannot be negative.");
        return column == 0 && row == 0 && layer == 0 ? ZERO :
                new AoESymmetrical(column, row, layer, column, row, layer);
    }

    public Mutable toMutable() {
        return new Mutable(maxColumn, maxRow, maxLayer, column, row, layer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        // noinspection PatternVariableHidesField
        if (!(o instanceof AoESymmetrical(int maxColumn, int maxRow, int maxLayer, int column, int row, int layer))) {
            return false;
        }

        return this.maxColumn == maxColumn && this.maxRow == maxRow && this.maxLayer == maxLayer &&
                this.column == column && this.row == row && this.layer == layer;
    }

    @Override
    public int hashCode() {
        int result = maxColumn;
        result = 31 * result + maxRow;
        result = 31 * result + maxLayer;
        result = 31 * result + column;
        result = 31 * result + row;
        result = 31 * result + layer;
        return result;
    }

    @Accessors(fluent = true, chain = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Mutable {

        @Getter
        @Setter
        public int maxColumn, maxRow, maxLayer;
        @Getter
        @Setter
        public int column, row, layer;

        public Mutable increaseColumn() {
            if (column < maxColumn) {
                column++;
            }
            return this;
        }

        public Mutable increaseRow() {
            if (row < maxRow) {
                row++;
            }
            return this;
        }

        public Mutable increaseLayer() {
            if (layer < maxLayer) {
                layer++;
            }
            return this;
        }

        public Mutable decreaseColumn() {
            if (column > 0) {
                column--;
            }
            return this;
        }

        public Mutable decreaseRow() {
            if (row > 0) {
                row--;
            }
            return this;
        }

        public Mutable decreaseLayer() {
            if (layer > 0) {
                layer--;
            }
            return this;
        }

        public AoESymmetrical toImmutable() {
            return new AoESymmetrical(maxColumn, maxRow, maxLayer, column, row, layer);
        }
    }
}
