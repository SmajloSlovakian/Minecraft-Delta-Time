package smsk.deltatime;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class DT implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("DeltaTime");
	public static MinecraftServer mc;
	public static long lastTickDuration; //nanos
	public static long nanosoffset;
	public static int tickstoadd;

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			
			var builder=literal("deltatime");

			builder=builder.then(literal("tickdelta").executes(c->{
				c.getSource().sendFeedback(() -> Text.literal("Previous tick duration was: "+lastTickDuration+" μs"), false);
				return((int)lastTickDuration/1000);
			}));

			builder=builder.then(literal("tickstoadd").executes(c->{
				c.getSource().sendFeedback(() -> Text.literal("Ticks to add to the precise counter for this tick: "+tickstoadd), false);
				return((int)tickstoadd);
			}));

			builder=builder.then(literal("setitemcooldown").requires(c->{return(c.hasPermissionLevel(2));})
				.then(argument("targets", EntityArgumentType.players())
					.then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
						.then(argument("duration", IntegerArgumentType.integer()).executes(c->{
							for (ServerPlayerEntity i : EntityArgumentType.getPlayers(c, "targets")) {
								i.getItemCooldownManager().set(ItemStackArgumentType.getItemStackArgument(c, "item").getItem(),IntegerArgumentType.getInteger(c, "duration"));
							}
							c.getSource().sendFeedback(()->Text.literal("Successfully changed cooldown."), true);
							return(1);
						}))
					)
				)
			);

			builder=builder.then(literal("setability").requires(c->{return(c.hasPermissionLevel(2));})
				.then(argument("targets",EntityArgumentType.players())
					.then(literal("flying"		).then(argument("value",BoolArgumentType.bool()).executes(c->{return(setAbilityCommand(c, "flying"));})))
					.then(literal("instabuild"	).then(argument("value",BoolArgumentType.bool()).executes(c->{return(setAbilityCommand(c, "instabuild"));})))
					.then(literal("invulnerable").then(argument("value",BoolArgumentType.bool()).executes(c->{return(setAbilityCommand(c, "invulnerable"));})))
					.then(literal("mayBuild"	).then(argument("value",BoolArgumentType.bool()).executes(c->{return(setAbilityCommand(c, "mayBuild"));})))
					.then(literal("mayfly"		).then(argument("value",BoolArgumentType.bool()).executes(c->{return(setAbilityCommand(c, "mayfly"));})))
					.then(literal("flySpeed"	).then(argument("value",FloatArgumentType.floatArg()).executes(c->{return(setAbilityCommand(c, "flySpeed"));})))
					.then(literal("walkSpeed"	).then(argument("value",FloatArgumentType.floatArg()).executes(c->{return(setAbilityCommand(c, "walkSpeed"));})))
				)
			);

			builder=builder.then(literal("setproperty")
				.then(argument("targets",EntityArgumentType.players())
					.then(literal("health"				).then(argument("value",FloatArgumentType.floatArg()).executes(c->{return(setPropertyCommand(c, "health"));})))
					.then(literal("foodExhaustionLevel"	).then(argument("value",FloatArgumentType.floatArg()).executes(c->{return(setPropertyCommand(c, "foodExhaustionLevel"));})))
					.then(literal("foodSaturationLevel"	).then(argument("value",FloatArgumentType.floatArg()).executes(c->{return(setPropertyCommand(c, "foodSaturationLevel"));})))
					.then(literal("foodLevel"			).then(argument("value",IntegerArgumentType.integer()).executes(c->{return(setPropertyCommand(c, "foodLevel"));})))
					//.then(literal("SelectedItemSlot"	).then(argument("value",IntegerArgumentType.integer()).executes(c->{return(setPropertyCommand(c, "SelectedItemSlot"));})))
					//.then(literal("foodTickTimer"		).then(argument("value",IntegerArgumentType.integer()).executes(c->{return(setPropertyCommand(c, "foodTickTimer"));})))
				)
			);

			dispatcher.register(builder);
		});
	}
	static int setPropertyCommand(CommandContext<ServerCommandSource> c,String property) throws CommandSyntaxException{
		for (ServerPlayerEntity i : EntityArgumentType.getPlayers(c, "targets")) {
			if     (property=="health")i.setHealth(FloatArgumentType.getFloat(c, "value"));
			else if(property=="foodExhaustionLevel")i.getHungerManager().setExhaustion(FloatArgumentType.getFloat(c, "value"));
			else if(property=="foodSaturationLevel")i.getHungerManager().setSaturationLevel(FloatArgumentType.getFloat(c, "value"));
			else if(property=="foodLevel")i.getHungerManager().setFoodLevel(IntegerArgumentType.getInteger(c, "value"));
			//else if(property=="SelectedItemSlot")i.getInventory().selectedSlot=IntegerArgumentType.getInteger(c, "value");
			//else if(property=="foodTickTimer")i.getHungerManager()(IntegerArgumentType.getInteger(c, "value"));
		}
		c.getSource().sendFeedback(()->Text.literal("Successfuly changed property."), false);
		return(1);
	}

	static int setAbilityCommand(CommandContext<ServerCommandSource> c,String ability) throws CommandSyntaxException{
		for (ServerPlayerEntity i : EntityArgumentType.getPlayers(c, "targets")) {
			if(ability=="flying")i.getAbilities().flying=BoolArgumentType.getBool(c, "value");
			else if(ability=="instabuild")i.getAbilities().creativeMode=BoolArgumentType.getBool(c, "value");
			else if(ability=="invulnerable")i.getAbilities().invulnerable=BoolArgumentType.getBool(c, "value");
			else if(ability=="mayBuild")i.getAbilities().allowModifyWorld=BoolArgumentType.getBool(c, "value");
			else if(ability=="mayfly")i.getAbilities().allowFlying=BoolArgumentType.getBool(c, "value");
			else if(ability=="flySpeed")i.getAbilities().setFlySpeed(FloatArgumentType.getFloat(c, "value"));
			else if(ability=="walkSpeed")i.getAbilities().setWalkSpeed(FloatArgumentType.getFloat(c, "value"));
			i.sendAbilitiesUpdate();
		}
		c.getSource().sendFeedback(()->Text.literal("Successfuly changed ability."), true);

		return(1);
	}

	public static void print(Object s){
		LOGGER.info(s+"");
	}
}
