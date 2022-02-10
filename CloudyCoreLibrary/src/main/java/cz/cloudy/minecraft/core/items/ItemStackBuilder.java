/*
  User: Cloudy
  Date: 08/02/2022
  Time: 14:45
*/

package cz.cloudy.minecraft.core.items;

import com.google.common.base.Preconditions;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

/**
 * @author Cloudy
 */
@Component
public class ItemStackBuilder {
    public static class Builder {
        private Material           material;
        private byte               amount = 1;
        private Consumer<ItemMeta> itemMetaConsumer;

        public Builder material(Material material) {
            this.material = material;
            return this;
        }

        public Builder amount(byte amount) {
            this.amount = amount;
            return this;
        }

        public Builder amount(int amount) {
            this.amount = (byte) amount;
            return this;
        }

        public Builder itemMeta(Consumer<ItemMeta> consumer) {
            this.itemMetaConsumer = consumer;
            return this;
        }

        public <T extends ItemMeta> Builder itemMeta(Class<T> clazz, Consumer<T> consumer) {
            this.itemMetaConsumer = itemMeta -> consumer.accept((T) itemMeta);
            return this;
        }

        public ItemStack build() {
            Preconditions.checkState(material!=null, "Material must be set");
            ItemStack itemStack = new ItemStack(material, amount);
            if (itemMetaConsumer != null)
                itemStack.editMeta(itemMetaConsumer);
            return itemStack;
        }
    }

    public Builder create() {
        return new Builder();
    }
}
