package fr.ralala.bleconnector.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import fr.ralala.bleconnector.R;

/********************************************************************************
 * <p><b>Project BleConnector</b><br/>
 * UI Helper functions
 * </p>
 *
 * @author Keidan
 * <p>
 *******************************************************************************/
public class UIHelper {

  /**
   * Displays a circular progress dialog.
   * @param context The Android context.
   * @param cancel The cancel event callback (if null the dialog is not cancelable).
   * @return AlertDialog
   */
  public static AlertDialog showCircularProgressDialog(Context context,DialogInterface.OnCancelListener cancel) {
    LayoutInflater layoutInflater = LayoutInflater.from(context);
    final ViewGroup nullParent = null;
    View view = layoutInflater.inflate(R.layout.circular_progress, nullParent);
    AlertDialog progress = new AlertDialog.Builder(context).create();
    if(cancel != null) {
      progress.setOnCancelListener(cancel);
      progress.setCancelable(true);
    } else
      progress.setCancelable(false);
    progress.setView(view);
    return progress;
  }

  /**
   * Shake a view on error.
   * @param owner The owner view.
   * @param errText The error text.
   */
  public static void shakeError(TextView owner, String errText) {
    TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
    shake.setDuration(500);
    shake.setInterpolator(new CycleInterpolator(5));
    if(owner != null) {
      if(errText != null)
        owner.setError(errText);
      owner.clearAnimation();
      owner.startAnimation(shake);
    }
  }

  /**
   * Displays an alert dialog.
   *
   * @param c       The Android context.
   * @param title   The alert dialog title.
   * @param message The alert dialog message.
   * @param click   Click on the ok button (The callback parameter is always null).
   */
  public static void showAlertDialog(final Context c, final int title, final String message, final View.OnClickListener click) {
    AlertDialog alertDialog = new AlertDialog.Builder(c).create();
    alertDialog.setTitle(c.getResources().getString(title));
    alertDialog.setMessage(message);
    alertDialog.setCancelable(click == null);
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,
        c.getResources().getString(R.string.ok), (dialog, which) -> {
          if (click != null)
            click.onClick(null);
          dialog.dismiss();
        });
    alertDialog.show();
  }

  /**
   * Opens a snack.
   *
   * @param activity The associated activity.
   * @param msg      The snack message.
   */
  public static void snackInfo(final Activity activity, String msg) {
    snack(activity, msg, null, null);
  }

  /**
   * Opens a snack.
   *
   * @param activity      The associated activity.
   * @param msg           The snack message.
   * @param actionLabel   null for default label (Hide), the label.
   * @param clickListener Click listener (view = null ; snackbar.dismiss() is called after the event).
   */
  public static void snack(final Activity activity, String msg, String actionLabel, View.OnClickListener clickListener) {
    final View cl = activity.findViewById(R.id.containerLayout);
    final Snackbar snackbar = Snackbar
        .make(cl, msg, Snackbar.LENGTH_LONG);
    snackbar.setAction(
        actionLabel == null ? activity.getString(R.string.snack_hide) : actionLabel, (view) -> {
          if (clickListener != null)
            clickListener.onClick(null);
          snackbar.dismiss();
        });
    snackbar.show();
  }
}
