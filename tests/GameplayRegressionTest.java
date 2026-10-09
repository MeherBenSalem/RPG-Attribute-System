package tn.nightbeam.ras;

import java.nio.file.*;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.*;
import tn.nightbeam.ras.config.*;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.*;

/** Actual gameplay/config source with minimal Minecraft/platform test doubles.
 * This is a regression harness, not in-game or loader/event integration evidence. */
public final class GameplayRegressionTest {
    private static int checks;
    public static void main(String[] args) throws Exception {
        Path directory=Files.createTempDirectory("ras-gameplay-test-");
        Services.CONFIG.directory=directory;
        try {
            testFreshAndImportedConfigs(directory);
            testKillAndSharing(directory);
            testMigrationDeathAndFiniteAmounts();
            System.out.println("PASS GameplayRegressionTest: " + checks + " assertions (actual config/gameplay source, test doubles)");
        } finally {
            try (var paths=Files.walk(directory)) { for(Path p:paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(p); }
        }
    }
    private static void testFreshAndImportedConfigs(Path directory) throws Exception {
        ConfigInitializer.init();
        String fresh=Files.readString(directory.resolve("ras/mob_xp.json"));
        check(!MobXpRules.parse(fresh).rules().enabled(), "fresh extension disabled");
        check(Services.CONFIG.getStringArray("ras","stats_display","totals").stream().anyMatch(s->s.contains("Total Attack Speed Bonus")), "fresh attribute3 label corrected");
        Path settings=directory.resolve("ras/settings.json");
        String importedSettings="{\"strict_config_mode\":true,\"use_vanilla_xp\":false,\"custom\":{\"keep\":7}}";
        String importedDrop="{\"default_vp_rates\":2.5,\"bosses_list\":[\"pack:dragon\"],\"min_drop_rate\":42,\"max_drop_rate\":55,\"dimensions_drop_rates\":[\"pack:moon/3\"],\"custom\":\"keep\"}";
        String importedStats="{\"totals\":[\"[label]My Mana[labelEnd][ids]3[idsEnd][mode]bonus[modeEnd]\"],\"gui_shadow_color\":\"#123456\",\"custom\":42}";
        String importedMob="{\"schema_version\":1,\"enabled\":false,\"extension\":{\"keep\":42},\"rules\":[]}";
        Files.writeString(settings,importedSettings);
        Files.writeString(directory.resolve("ras/droprate.json"),importedDrop);
        Files.writeString(directory.resolve("ras/stats_display.json"),importedStats);
        Files.writeString(directory.resolve("ras/mob_xp.json"),importedMob);
        ConfigInitializer.init();
        check(Files.readString(settings).equals(importedSettings), "imported strict settings preserved");
        check(Files.readString(directory.resolve("ras/droprate.json")).equals(importedDrop), "imported rates and loader-specific boss item-drop fields preserved");
        check(Files.readString(directory.resolve("ras/stats_display.json")).equals(importedStats), "imported custom stats labels/colors preserved");
        check(Files.readString(directory.resolve("ras/mob_xp.json")).equals(importedMob), "imported mob unknown keys preserved");
        Services.CONFIG.setStringValue("ras","settings","validation_mode","fail");
        Files.writeString(directory.resolve("ras/mob_xp.json"),"{\"schema_version\":99}");
        check(ConfigValidator.run().shouldAbortStartup(), "unsupported schema strict validation fails");
        Services.CONFIG.setStringValue("ras","settings","validation_mode","warn");
        check(!ConfigValidator.run().shouldAbortStartup(), "unsupported schema warn validation allows legacy fallback");
        Files.writeString(directory.resolve("ras/mob_xp.json"),fresh);
        check(MobXpConfig.reload().loaded(), "startup defaults load");
    }
    private static void testKillAndSharing(Path directory) throws Exception {
        Services.CONFIG.setBooleanValue("ras","settings","use_vanilla_xp",false);
        Services.CONFIG.setBooleanValue("ras","settings","allowSummonXP",true);
        Services.CONFIG.setBooleanValue("ras","settings","shared_xp_enabled",true);
        Services.CONFIG.setNumberValue("ras","settings","shared_xp_radius",16);
        Services.CONFIG.setNumberValue("ras","settings","shared_xp_percentage",50);
        Services.CONFIG.setNumberValue("ras","settings","max_player_level",20);
        Services.CONFIG.setNumberValue("ras","settings","exp_curve_max_level",20);
        Services.CONFIG.setNumberValue("ras","settings","exp_curve_first_level_xp",100);
        Services.CONFIG.setNumberValue("ras","settings","exp_curve_default_scale",1);
        Services.CONFIG.setStringArray("ras","settings","exp_curve_scale_intervals",List.of());
        Services.CONFIG.setNumberValue("ras","droprate","default_vp_rates",9);
        Services.CONFIG.setStringArray("ras","droprate","dimensions_drop_rates",List.of("minecraft:overworld/1.5"));
        ServerLevel world=new ServerLevel("minecraft:overworld");
        ServerPlayer owner=new ServerPlayer(world,0); ServerPlayer near=new ServerPlayer(world,4);
        ServerPlayer edge=new ServerPlayer(world,16);ServerPlayer far=new ServerPlayer(world,17);
        ServerLevel otherWorld=new ServerLevel("pack:moon");ServerPlayer other=new ServerPlayer(otherWorld,0);
        LivingEntity zombie=new LivingEntity(world,"minecraft:zombie",20,10);
        GameplayRulesProcedure.handleEntityKill(world,owner,zombie);
        eq(15,vars(owner).totalXp,"owner retains unshared half");eq(7.5,vars(near).totalXp,"near share");
        eq(7.5,vars(edge).totalXp,"radius edge inclusive");eq(0,vars(far).totalXp,"outside radius");
        eq(0,vars(other).totalXp,"other dimension excluded");eq(30,sum(owner,near,edge,far,other),"disabled extension pool conserved, dimension replaces global");
        String weighted="{\"schema_version\":1,\"enabled\":true,\"difficulty_weighting\":{\"enabled\":true,\"hard\":2},"
                + "\"armor_weighting\":{\"enabled\":true,\"per_armor_point\":0.1,\"max_multiplier\":5},\"rules\":["
                + "{\"id\":\"tag\",\"tag\":\"pack:boss\",\"priority\":99,\"base_xp\":1000},"
                + "{\"id\":\"entity\",\"entity\":\"minecraft:zombie\",\"base_xp\":40,\"multiplier\":1.25}]}";
        Files.writeString(directory.resolve("ras/mob_xp.json"),weighted);check(MobXpConfig.reload().loaded(),"weighted reload");
        zombie.getType().tags.add("pack:boss");world.difficulty="hard";
        GameplayRulesProcedure.handleEntityKill(world,owner,zombie);
        eq(165,vars(owner).totalXp,"entity winner weighted owner share");
        eq(82.5,vars(near).totalXp,"weighted near share");eq(330,sum(owner,near,edge,far,other),"weighted pool conserved");
        Services.CONFIG.setBooleanValue("ras","settings","use_vanilla_xp",true);
        GameplayRulesProcedure.handleEntityKill(world,owner,zombie);eq(330,sum(owner,near,edge,far,other),"vanilla XP bypass preserved");
        Services.CONFIG.setBooleanValue("ras","settings","use_vanilla_xp",false);
        world.client=true;GameplayRulesProcedure.handleEntityKill(world,owner,zombie);world.client=false;
        eq(330,sum(owner,near,edge,far,other),"client never awards XP");
        Services.CONFIG.setBooleanValue("ras","settings","shared_xp_enabled",false);
        Summon summon=new Summon(world,owner);
        GameplayRulesProcedure.handleEntityKill(world,summon,zombie);eq(465,vars(owner).totalXp,"summon owner receives complete XP when sharing disabled");
        Services.CONFIG.setBooleanValue("ras","settings","allowSummonXP",false);
        GameplayRulesProcedure.handleEntityKill(world,summon,zombie);eq(465,vars(owner).totalXp,"summon disabled ignores kill");
        Services.CONFIG.setBooleanValue("ras","settings","shared_xp_enabled",true);
        Services.CONFIG.setNumberValue("ras","settings","shared_xp_radius",0);
        GameplayRulesProcedure.handleEntityKill(world,owner,zombie);eq(765,vars(owner).totalXp,"zero sharing radius leaves owner full XP");
        Services.CONFIG.setNumberValue("ras","settings","shared_xp_radius",16);
        Services.CONFIG.setNumberValue("ras","settings","shared_xp_percentage",100);
        GameplayRulesProcedure.handleEntityKill(world,owner,zombie);eq(765,vars(owner).totalXp,"100 percent shared owner zero");
        eq(232.5,vars(near).totalXp,"100 percent shared recipient amount");
        double total=sum(owner,near,edge,far,other);
        Files.writeString(directory.resolve("ras/mob_xp.json"),"{\"schema_version\":1,\"enabled\":true,\"rules\":["
                +"{\"id\":\"overflow\",\"entity\":\"minecraft:zombie\",\"base_xp\":1e308,\"multiplier\":1e308}]}");
        MobXpConfig.reload();GameplayRulesProcedure.handleEntityKill(world,owner,zombie);
        eq(total,sum(owner,near,edge,far,other),"overflow kill does not poison multiplayer progress");
        GameplayRulesProcedure.handleEntityKill(world,null,zombie);GameplayRulesProcedure.handleEntityKill(world,owner,null);
        eq(total,sum(owner,near,edge,far,other),"null event ignored");
        Files.writeString(directory.resolve("ras/mob_xp.json"),"{\"schema_version\":1,\"enabled\":true,\"rules\":["
                +"{\"id\":\"tag\",\"tag\":\"pack:dynamic\",\"base_xp\":50}]}");MobXpConfig.reload();
        eq(30,MobXpConfig.calculate(zombie,1.5),"adapter unmatched tag baseline");
        zombie.getType().tags.add("pack:dynamic");eq(75,MobXpConfig.calculate(zombie,1.5),"adapter reads current tag membership");
        Files.writeString(directory.resolve("ras/mob_xp.json"),"{\"schema_version\":1,\"enabled\":true,\"armor_weighting\":{\"enabled\":true,\"per_armor_point\":0.1},\"rules\":[]}");
        MobXpConfig.reload();
        LivingEntity fractional=new LivingEntity(world,"pack:fractional",20,2.5);
        eq(37.5,MobXpConfig.calculate(fractional,1.5),"adapter weights fractional effective armor attribute");
    }
    public static final class Summon extends Entity {
        private final ServerPlayer owner;
        public Summon(ServerLevel level,ServerPlayer owner){super(level,"pack:summon");this.owner=owner;}
        public Player getOwner(){return owner;}
    }
    private static void testMigrationDeathAndFiniteAmounts() {
        ServerPlayer migrated=new ServerPlayer(new ServerLevel("minecraft:overworld"),0);
        PlayerVariables vars=vars(migrated);vars.totalXp=-1;vars.Level=2;vars.currentXpTLevel=25;vars.pointsGrantedThroughLevel=-1;
        LevelingService.initializeOrMigrate(migrated);eq(225,vars.totalXp,"legacy migration derives total");eq(2,vars.Level,"legacy level retained");
        LevelingService.addXp(migrated,Double.NaN);LevelingService.addXp(migrated,Double.POSITIVE_INFINITY);
        LevelingService.setTotalXp(migrated,Double.NaN);eq(225,vars.totalXp,"invalid command or API amount ignored");
        LevelingService.addXp(migrated,75);eq(3,vars.Level,"actual level calculation");
        double points=vars.SparePoints;LevelingService.initializeOrMigrate(migrated);eq(points,vars.SparePoints,"repeat sync does not duplicate level points");
        LevelingService.resetProgress(migrated);eq(0,vars.totalXp,"death/reset total cleared");eq(0,vars.Level,"death/reset level cleared");
        LevelingService.addXp(migrated,25);eq(25,vars.totalXp,"new kill after reset awards fresh total");
        LevelingService.setTotalXp(migrated,Double.MAX_VALUE);double finite=vars.totalXp;
        LevelingService.addXp(migrated,Double.MAX_VALUE);eq(finite,vars.totalXp,"total overflow rejected");
    }
    private static PlayerVariables vars(Entity entity){return Services.PLATFORM.getPlayerVariables(entity);}
    private static double sum(Entity... players){return Arrays.stream(players).mapToDouble(p->vars(p).totalXp).sum();}
    private static void eq(double expected,double actual,String label){check(Double.isFinite(actual)&&Math.abs(expected-actual)<=Math.max(1e-9,Math.abs(expected)*1e-12),label+": "+expected+" != "+actual);}
    private static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
}
