package me.mrgraycat.eglow.command.subcommand.impl.admin;

import me.mrgraycat.eglow.command.subcommand.SubCommand;
import me.mrgraycat.eglow.config.EGlowMainConfig.MainConfig;
import me.mrgraycat.eglow.config.EGlowMessageConfig.Message;
import me.mrgraycat.eglow.manager.DataManager;
import me.mrgraycat.eglow.manager.glow.IEGlowEffect;
import me.mrgraycat.eglow.manager.glow.IEGlowPlayer;
import me.mrgraycat.eglow.util.Common.GlowDisableReason;
import me.mrgraycat.eglow.util.Common.GlowVisibility;
import me.mrgraycat.eglow.util.chat.ChatUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SetCommand extends SubCommand {

	@Override
	public String getName() {
		return "set";
	}

	@Override
	public String getDescription() {
		return "Set an effect for a player/NPC";
	}

	@Override
	public String getPermission() {
		return "eglow.command.set";
	}

	@Override
	public String[] getSyntax() {
		return new String[]{"/eGlow set <player/npc> <color>",
				"/eGlow set <player/npc> blink <color> <speed>",
				"/eGlow set <player/npc> <effect> <speed>",
				"/eGlow set <player/npc> glowonjoin <true/false>"};
	}

	@Override
	public boolean isPlayerCmd() {
		return false;
	}

	@Override
	public void perform(CommandSender commandSender, IEGlowPlayer eGlowPlayer, String[] args) {
		List<IEGlowPlayer> eTargets = getTarget(commandSender, args);

		if (eTargets == null) {
			sendSyntax(commandSender);
			return;
		}

		for (IEGlowPlayer eTarget : eTargets) {
			IEGlowEffect effect = null;

			if (eTarget == null)
				continue;

			switch (args.length) {
				case (3):
					effect = DataManager.getEGlowEffect(args[2].toLowerCase().replace("off", "none").replace("disable", "none"));
					break;
				case (4):
					if (args[2].equalsIgnoreCase("glowonjoin")) {
						eTarget.setGlowOnJoin(Boolean.parseBoolean(args[3].toLowerCase()));
						ChatUtil.sendMessage(commandSender, Message.OTHER_GLOW_ON_JOIN_CONFIRM.get(eTarget, args[3].toLowerCase()), true);
						continue;

					}
					effect = DataManager.getEGlowEffect(args[2] + args[3]);
					break;
				case (5):
					effect = DataManager.getEGlowEffect(args[2] + args[3] + args[4]);
					break;
			}

			if (effect == null) {
				sendSyntax(commandSender);
				return;
			}

			if (eTarget.getEntityType().equals("PLAYER")) {
				if (eTarget.getGlowDisableReason().equals(GlowDisableReason.DISGUISE)) {
					ChatUtil.sendMessage(commandSender, Message.OTHER_PLAYER_DISGUISE.get(), true);
					continue;
				}

				if (eTarget.isInvisible()) {
					ChatUtil.sendMessage(commandSender, Message.OTHER_PLAYER_INVISIBLE.get(), true);
					continue;
				}

				if (eTarget.isInBlockedWorld()) {
					ChatUtil.sendMessage(commandSender, Message.OTHER_PLAYER_IN_DISABLED_WORLD.get(), true);
					continue;
				}
			}

			if (effect.getName().equals("none")) {
				if (eTarget.isGlowing())
					eTarget.disableGlow(false);

				if (eTarget.getEntityType().equals("PLAYER") && MainConfig.SETTINGS_NOTIFICATIONS_TARGET_COMMAND.getBoolean() && !eTarget.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
					ChatUtil.sendMessage(eTarget.getPlayer(), Message.TARGET_NOTIFICATION_PREFIX.get() + Message.DISABLE_GLOW.get(), true);
				ChatUtil.sendMessage(commandSender, Message.OTHER_CONFIRM_OFF.get(eTarget), true);
				continue;
			}

			if (!eTarget.isSameGlow(effect)) {
				eTarget.disableGlow(true);
				eTarget.activateGlow(effect);

				if (eTarget.getEntityType().equals("PLAYER") && MainConfig.SETTINGS_NOTIFICATIONS_TARGET_COMMAND.getBoolean() && !eTarget.getGlowVisibility().equals(GlowVisibility.UNSUPPORTEDCLIENT))
					ChatUtil.sendMessage(eTarget.getPlayer(), Message.TARGET_NOTIFICATION_PREFIX.get() + Message.NEW_GLOW.get(effect.getDisplayName()), true);
			}

			ChatUtil.sendMessage(commandSender, Message.OTHER_CONFIRM.get(eTarget, effect.getDisplayName()), true);
		}
	}
}