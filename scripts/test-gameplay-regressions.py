#!/usr/bin/env python3
"""Run actual config/gameplay source against minimal Minecraft/platform doubles.

This isolates regression semantics; it does not prove loader wiring or in-game UI.
Requires a cached Gson jar; no downloads or EULA acceptance.
"""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--gson-jar',default=os.environ.get('GSON_JAR'))
parser.add_argument('--javac',default='javac')
parser.add_argument('--java',default='java')
args=parser.parse_args()
if not args.gson_jar or not Path(args.gson_jar).is_file(): parser.error('Supply --gson-jar PATH or GSON_JAR')
repo=Path(__file__).resolve().parents[1]
stubs={
'net/minecraft/world/entity/ai/attributes/Attributes.java':"package net.minecraft.world.entity.ai.attributes; public class Attributes {public static final Object ARMOR=new Object();}",
'net/minecraft/world/entity/ai/attributes/AttributeInstance.java':"package net.minecraft.world.entity.ai.attributes; public record AttributeInstance(double value) {public double getValue(){return value;}}",
'net/minecraft/resources/ResourceLocation.java':'''package net.minecraft.resources;
public record ResourceLocation(String value) { public static ResourceLocation tryParse(String s){return new ResourceLocation(s);} public String toString(){return value;} }''',
'net/minecraft/resources/Identifier.java':'''package net.minecraft.resources;
public record Identifier(String value) { public static Identifier tryParse(String s){return new Identifier(s);} public String toString(){return value;} }''',
'net/minecraft/tags/TagKey.java':'''package net.minecraft.tags;
public record TagKey(String value) {public static TagKey create(Object registry,Object id){return new TagKey(id.toString());}}''',
'net/minecraft/core/registries/Registries.java':'''package net.minecraft.core.registries;
public class Registries { public static final Object ENTITY_TYPE=new Object(); }''',
'net/minecraft/core/registries/BuiltInRegistries.java':'''package net.minecraft.core.registries;
public class BuiltInRegistries {public static final Registry ENTITY_TYPE=new Registry();public static final Registry BLOCK=new Registry();
public static class Registry {public Object getKey(Object object){return object.toString();}}}''',
'net/minecraft/network/chat/Component.java':'''package net.minecraft.network.chat;
public record Component(String value){public static Component translatable(String key,Object... args){return new Component(key);}}''',
'net/minecraft/world/level/LevelAccessor.java':'''package net.minecraft.world.level;
public interface LevelAccessor {boolean isClientSide();}''',
'net/minecraft/world/level/Level.java':'''package net.minecraft.world.level;
public class Level implements LevelAccessor { public boolean client;public String difficulty="normal";private final String dimension;
public Level(String dimension){this.dimension=dimension;}public boolean isClientSide(){return client;}
public Dimension dimension(){return new Dimension(dimension);}public Difficulty getDifficulty(){return new Difficulty(difficulty);}
public record Dimension(String value){public Object location(){return value;}public String toString(){return "ResourceKey[minecraft:dimension / "+value+"]";}}
public record Difficulty(String value){public String getKey(){return value;}}}''',
'net/minecraft/world/level/block/state/BlockState.java':'''package net.minecraft.world.level.block.state;
public class BlockState {public Object getBlock(){return "minecraft:stone";}}''',
'net/minecraft/world/entity/EntityType.java':'''package net.minecraft.world.entity;
import java.util.*;import net.minecraft.tags.TagKey;
public class EntityType {public final Set<String> tags=new HashSet<>();private final String id;public EntityType(String id){this.id=id;}
public EntityType builtInRegistryHolder(){return this;}public boolean is(TagKey tag){return tags.contains(tag.value());}public String toString(){return id;}}''',
'net/minecraft/world/entity/Entity.java':'''package net.minecraft.world.entity;
import java.util.UUID;import net.minecraft.world.level.Level;
public class Entity {private final Level level;private final EntityType type;private final UUID uuid=UUID.randomUUID();
public Entity(Level level,String id){this.level=level;this.type=new EntityType(id);}public Level level(){return level;}
public EntityType getType(){return type;}public UUID getUUID(){return uuid;}}''',
'net/minecraft/world/entity/LivingEntity.java':'''package net.minecraft.world.entity;
import net.minecraft.world.level.Level;
public class LivingEntity extends Entity {private final float health;private final double armor;
public LivingEntity(Level level,String id,float health,double armor){super(level,id);this.health=health;this.armor=armor;}
public float getMaxHealth(){return health;}public int getArmorValue(){return (int)armor;}public net.minecraft.world.entity.ai.attributes.AttributeInstance getAttribute(Object attr){return new net.minecraft.world.entity.ai.attributes.AttributeInstance(armor);}}''',
'net/minecraft/world/entity/player/Player.java':'''package net.minecraft.world.entity.player;
import net.minecraft.world.entity.LivingEntity;import net.minecraft.world.level.Level;import net.minecraft.network.chat.Component;
public class Player extends LivingEntity {public double position;public Player(Level level,double position){super(level,"minecraft:player",20,0);this.position=position;}
public double distanceToSqr(Player p){return Math.pow(position-p.position,2);}public void displayClientMessage(Component c,boolean action){}public void sendSystemMessage(Component c){}}''',
'net/minecraft/server/level/ServerLevel.java':'''package net.minecraft.server.level;
import java.util.*;import net.minecraft.world.level.Level;import net.minecraft.world.entity.player.Player;
public class ServerLevel extends Level {public final List<ServerPlayer> players=new ArrayList<>();public ServerLevel(String id){super(id);}
public List<ServerPlayer> players(){return players;}public Player getPlayerByUUID(UUID uuid){return players.stream().filter(p->p.getUUID().equals(uuid)).findFirst().orElse(null);}}''',
'net/minecraft/server/level/ServerPlayer.java':'''package net.minecraft.server.level;
import net.minecraft.world.entity.player.Player;
public class ServerPlayer extends Player {public ServerPlayer(ServerLevel level,double position){super(level,position);level.players.add(this);}}''',
 'tn/nightbeam/ras/network/PlayerVariables.java':'''package tn.nightbeam.ras.network;
import java.util.*;public class PlayerVariables {public double totalXp=0,Level,currentXpTLevel,nextevelXp=100,pointsGrantedThroughLevel=0,SparePoints;
public Map<String,Double> attributes=new HashMap<>(),attributePoints=new HashMap<>();}''',
 'tn/nightbeam/ras/platform/Services.java':'''package tn.nightbeam.ras.platform;
import java.nio.file.*;import java.util.*;import net.minecraft.world.entity.Entity;import tn.nightbeam.ras.network.PlayerVariables;
public class Services {public static final TestConfigService CONFIG=new TestConfigService(Path.of("."));public static final Platform PLATFORM=new Platform();
public static class Platform {private final Map<Entity,PlayerVariables> variables=new IdentityHashMap<>();public PlayerVariables getPlayerVariables(Entity entity){return variables.computeIfAbsent(entity,key->new PlayerVariables());}
public void syncPlayerVariables(PlayerVariables vars,Entity entity){}}}''',
 'tn/nightbeam/ras/Constants.java':'''package tn.nightbeam.ras;
public class Constants {public static final Log LOG=new Log();public static class Log {public void warn(String s,Object... args){}public void info(String s,Object... args){}public void error(String s,Object... args){}}}''',
 'tn/nightbeam/ras/util/AttributeManager.java':'''package tn.nightbeam.ras.util;
import java.util.*;public class AttributeManager {public static List<String> getAttributeIds(){return List.of();}}''',
 'tn/nightbeam/ras/api/RespecOptions.java':'''package tn.nightbeam.ras.api;
public class RespecOptions {public static RespecOptions item(){return new RespecOptions();}}''',
 'tn/nightbeam/ras/procedures/RespecService.java':'''package tn.nightbeam.ras.procedures;
import net.minecraft.server.level.ServerPlayer;import tn.nightbeam.ras.api.RespecOptions;
public class RespecService {public static void tryRespec(ServerPlayer player,RespecOptions options){}}''',
 'tn/nightbeam/ras/procedures/CheckLevelupRewardsProcedure.java':'''package tn.nightbeam.ras.procedures;
import net.minecraft.world.level.Level;import net.minecraft.world.entity.Entity;
public class CheckLevelupRewardsProcedure {public static void execute(Level level,Entity entity){}}''',
 'tn/nightbeam/ras/procedures/OnPlayerSpawnProcedure.java':'''package tn.nightbeam.ras.procedures;
import net.minecraft.world.entity.Entity;public class OnPlayerSpawnProcedure {public static void resetAttributesToInitial(Entity e){}public static void execute(Entity e){}}''',
}
for root in ['1.20.1','1.21.1','26.1.2','26.2','26.3']:
    production=repo/root/'common/src/main/java'
    if root=='26.2':production/='java'
    package=production/'tn/nightbeam/ras'
    with tempfile.TemporaryDirectory(prefix='ras-gameplay-') as tmp:
        temp=Path(tmp);files=[]
        for name,source in stubs.items():
            path=temp/name;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(source);files.append(str(path))
        for name in ['platform/IConfigService.java','config/ConfigInitializer.java','config/ConfigValidator.java',
                     'config/MobXpRules.java','config/MobXpRuleStore.java','config/MobXpConfig.java',
                     'procedures/GameplayRulesProcedure.java','procedures/LevelingService.java','util/AttributeScaling.java']:
            files.append(str(package/name))
        files.extend([str(repo/'tests/GameplayRegressionTest.java'),str(repo/'tests/support/tn/nightbeam/ras/platform/TestConfigService.java')])
        classes=temp/'classes';classes.mkdir()
        subprocess.run([args.javac,'--release','17','-cp',args.gson_jar,'-d',str(classes),*files],check=True)
        subprocess.run([args.java,'-cp',str(classes)+os.pathsep+args.gson_jar,'tn.nightbeam.ras.GameplayRegressionTest'],check=True)
        print(f'PASS {root}: actual config/gameplay source with test doubles',flush=True)
