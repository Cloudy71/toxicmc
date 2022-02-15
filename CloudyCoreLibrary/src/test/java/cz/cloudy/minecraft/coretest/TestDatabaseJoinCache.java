package cz.cloudy.minecraft.coretest;

import cz.cloudy.minecraft.core.CorePlugin;
import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.componentsystem.annotations.ComponentScan;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.Column;
import cz.cloudy.minecraft.core.database.annotation.Join;
import cz.cloudy.minecraft.core.database.annotation.PrimaryKey;
import cz.cloudy.minecraft.core.database.annotation.Table;
import org.junit.jupiter.api.Test;

import java.util.Set;

@ComponentScan(classes = CorePlugin.class)
public class TestDatabaseJoinCache {
    @Table("__test_entity")
    static class TestEntity extends DatabaseEntity {
        @Column("num")
        @PrimaryKey
        protected int num;

        @Join(table = TestEntityJoin.class, where = "num = :num")
        public TestEntityJoin getSingleJoin() {
            return null;
        }

        @Join(table = TestEntityJoin.class, where = "TRUE")
        public Set<TestEntityJoin> getAllJoins() {
            return null;
        }
    }

    @Table("__test_entity_join")
    static class TestEntityJoin extends DatabaseEntity {
        @Column("num")
        @PrimaryKey
        protected int num;
    }

    private ComponentLoader componentLoader;

    //    @Test
    public void startDatabaseEngine() {
        System.out.println("Creating component loader");
        componentLoader = new ComponentLoader();
        System.out.println("Scanning for components");
        componentLoader.readComponentScansFromClass(TestDatabaseJoinCache.class);
    }
}
