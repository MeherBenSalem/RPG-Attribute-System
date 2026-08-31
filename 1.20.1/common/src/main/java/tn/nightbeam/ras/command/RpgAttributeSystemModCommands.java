package tn.nightbeam.ras.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import tn.nightbeam.ras.procedures.*;
import tn.nightbeam.ras.util.RasPermissions;

public class RpgAttributeSystemModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(Commands.literal("ras")
                .then(Commands.literal("add").then(Commands.literal("level")
                        .requires(RasPermissions::canAddLevel)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(1))
                                        .executes(arguments -> {
                                            LevelUpUserCommandProcedureProcedure.execute(arguments);
                                            return 0;
                                        }))))
                        .then(Commands.literal("attributes")
                                .then(Commands.argument("attribute_Id", DoubleArgumentType.doubleArg(1, 10))
                                        .then(Commands.argument("count", DoubleArgumentType.doubleArg())
                                                .executes(arguments -> {
                                                    Level world = arguments.getSource().getLevel();
                                                    Entity entity = arguments.getSource().getEntity();
                                                    AddPointsCmdProcedure.execute(world, arguments, entity);
                                                    return 0;
                                                })
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .executes(arguments -> {
                                                            GiveAttributesToPlayerProcedure.execute(arguments);
                                                            return 0;
                                                        }))))))
                .then(Commands.literal("xp").then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                        .executes(arguments -> {
                            Level world = arguments.getSource().getLevel();
                            double x = arguments.getSource().getPosition().x();
                            double y = arguments.getSource().getPosition().y();
                            double z = arguments.getSource().getPosition().z();
                            Entity entity = arguments.getSource().getEntity();
                            GiveXpCmdProcedure.execute(world, x, y, z, arguments, entity);
                            return 0;
                        }).then(Commands.argument("player", EntityArgument.player()).executes(arguments -> {
                            GiveXpToPlayerProcedure.execute(arguments);
                            return 0;
                        }))))
                .then(Commands.literal("set").then(Commands.literal("xp")
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg()).executes(arguments -> {
                            Level world = arguments.getSource().getLevel();
                            double x = arguments.getSource().getPosition().x();
                            double y = arguments.getSource().getPosition().y();
                            double z = arguments.getSource().getPosition().z();
                            Entity entity = arguments.getSource().getEntity();
                            SetXpCmdProcedure.execute(world, x, y, z, arguments, entity);
                            return 0;
                        }).then(Commands.argument("player", EntityArgument.player()).executes(arguments -> {
                            SetXpToPlayerProcedure.execute(arguments);
                            return 0;
                        })))))
                .then(Commands.literal("reset").executes(arguments -> {
                    Entity entity = arguments.getSource().getEntity();
                    ResetPlayerCmdProcedure.execute(entity);
                    return 0;
                }).then(Commands.argument("player", EntityArgument.player()).executes(arguments -> {
                    ResetGivenPlayerProcedure.execute(arguments);
                    return 0;
                })))
                .then(Commands.literal("unlock")
                        .then(Commands.argument("attribute", DoubleArgumentType.doubleArg(1, 10))
                                .executes(arguments -> {
                                    Entity entity = arguments.getSource().getEntity();
                                    UnlockAttributeProcedure.execute(arguments, entity);
                                    return 0;
                                }).then(Commands.argument("target", EntityArgument.player()).executes(arguments -> {
                                    UnlockAttributeTargetProcedure.execute(arguments);
                                    return 0;
                                }))))
                .then(Commands.literal("lock")
                        .then(Commands.argument("attribute", DoubleArgumentType.doubleArg(1, 10))
                                .executes(arguments -> {
                                    Entity entity = arguments.getSource().getEntity();
                                    LockAttributeProcedure.execute(arguments, entity);
                                    return 0;
                                }).then(Commands.argument("target", EntityArgument.player()).executes(arguments -> {
                                    LockAttributeTargetProcedure.execute(arguments);
                                    return 0;
                                }))))
                .then(Commands.literal("respec").executes(arguments -> {
                    RespecCmdProcedure.execute(arguments.getSource().getEntity());
                    return 0;
                }).then(Commands.argument("player", EntityArgument.player()).executes(arguments -> {
                    RespecPlayerCmdProcedure.execute(arguments);
                    return 0;
                })))
                .then(Commands.literal("template")
                        .then(Commands.literal("list").executes(arguments -> {
                            TemplateListCmdProcedure.execute(arguments);
                            return 0;
                        }))
                        .then(Commands.literal("apply")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(arguments -> {
                                            TemplateApplyCmdProcedure.executeSelf(arguments.getSource().getEntity(),
                                                    StringArgumentType.getString(arguments, "name"));
                                            return 0;
                                        })
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(arguments -> {
                                                    TemplateApplyCmdProcedure.executeOther(arguments,
                                                            StringArgumentType.getString(arguments, "name"));
                                                    return 0;
                                                })))))
                .then(Commands.literal("level")
                        .executes(arguments -> {
                            LevelCmdProcedure.executeSelf(arguments);
                            return 1;
                        })
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(arguments -> {
                                    LevelCmdProcedure.executeOther(arguments);
                                    return 1;
                                })))
                .then(Commands.literal("rewards")
                        .executes(arguments -> {
                            RewardsListCmdProcedure.execute(arguments);
                            return 1;
                        })
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                .executes(arguments -> {
                                    RewardsListCmdProcedure.execute(arguments,
                                            IntegerArgumentType.getInteger(arguments, "level"));
                                    return 1;
                                }))));
    }
}
