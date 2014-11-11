package cz.mammahelp.handy;

import static cz.mammahelp.handy.AndroidConstants.EXCEPTION;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;

public class NotificationUtils {

	private static final Logger log = LoggerFactory
			.getLogger(NotificationUtils.class);

	public NotificationUtils() {
	}

	public static void makeNotification(Context ctx, MammaHelpException e) {
		makeNotification(ctx, null,
				e.isHandled() ? R.drawable.ic_action_warning
						: R.drawable.ic_action_error, e);
	}

	public static void makeNotification(Context ctx, Class<?> clz,
			Integer icon, MammaHelpException e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		Intent intent = null;
		if (!e.isHandled()) {
			intent = new Intent(ctx, clz);
			Bundle b = new Bundle();
			b.putSerializable(EXCEPTION, e);
			intent.putExtras(b);
		}
		makeNotification(ctx, clz, getNotificationId(e), icon, null,
				R.string.app_name, e.toString(ctx), intent);
	}

	private static int getNotificationId(MammaHelpException e) {
		int id = e.getErrorType() == null ? 0 : e.getErrorType().ordinal();
		// TODO doplnit generovani ID zpravy.
		return id;
	}

	public static void makeNotification(Context ctx, Class<?> clz,
			int notifyID, Integer icon, int title, String description) {
		makeNotification(ctx, clz, notifyID, icon, null, title, description,
				new Intent(ctx, clz));
	}

	public static void makeNotification(Context ctx, Class<?> clz,
			int notifyID, Integer smallIcon, Bitmap largeIcon, int title,
			String description) {
		makeNotification(ctx, clz, notifyID, smallIcon, largeIcon, title,
				description, new Intent(ctx, clz));
	}

	public static void makeNotification(Context ctx, Class<?> clz,
			int notifyID, Integer icon, Bitmap largeIcon, int title,
			String description, Intent intent) {

		if (icon == null)
			icon = R.drawable.ic_action_warning;

		log.debug("iconid: " + icon);
		log.debug("iconname: " + ctx.getResources().getResourceName(icon));
		log.debug("iconname: " + ctx.getResources().getResourceEntryName(icon));
		log.debug(ctx.getResources().getDrawable(icon).toString());

		Builder builder = new NotificationCompat.Builder(ctx)
				.setTicker(ctx.getResources().getString(R.string.app_name))
				.setSmallIcon(icon)
				.setStyle(
						new NotificationCompat.BigTextStyle()
								.bigText(description))
				.setContentTitle(ctx.getResources().getString(title))
				.setContentText(description).setAutoCancel(true);
		if (largeIcon != null) {
			builder.setLargeIcon(largeIcon);
		}
		if (clz != null) {
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
			stackBuilder.addParentStack(clz);
			stackBuilder.addNextIntent(intent);
			builder.setContentIntent(stackBuilder.getPendingIntent(0,
					PendingIntent.FLAG_UPDATE_CURRENT));
			builder.setAutoCancel(true);
		}

		((NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE)).notify(
				notifyID, builder.build());

	}
}