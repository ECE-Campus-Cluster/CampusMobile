package fr.alexdet.android.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import fr.alexdet.android.R;

/**
 * QuickAction dialog.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 * 
 *         Contributors: - Kevin Peck <kevinwpeck@gmail.com>
 * 
 *         Adapted for MyECEParis : - Alexis de Talhouet
 */
public class QuickAction extends PopupWindows implements OnDismissListener {
	private ImageView mArrowUp;
	private LayoutInflater inflater;
	private ViewGroup mTrack;
	private OnActionItemClickListener mItemClickListener;
	private OnDismissListener mDismissListener;

	private List<ActionItem> mActionItemList = new ArrayList<ActionItem>();

	private boolean mDidAction;

	private int mChildPos;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            Context
	 */
	public QuickAction(Context context) {
		super(context);

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		setRootViewId(R.layout.quickaction);

		mChildPos = 0;
	}

	/**
	 * Get action item at an index
	 * 
	 * @param index
	 *            Index of item (position from callback)
	 * 
	 * @return Action Item at the position
	 */
	public ActionItem getActionItem(int index) {
		return mActionItemList.get(index);
	}

	/**
	 * Set root view.
	 * 
	 * @param id
	 *            Layout resource id
	 */
	public void setRootViewId(int id) {
		mRootView = (ViewGroup) inflater.inflate(id, null);
		mTrack = (ViewGroup) mRootView.findViewById(R.id.tracks);

		mArrowUp = (ImageView) mRootView.findViewById(R.id.arrow_up);

		mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		setContentView(mRootView);
	}

	/**
	 * Add action item
	 * 
	 * @param action
	 *            {@link ActionItem}
	 */
	public void addActionItem(ActionItem action) {
		mActionItemList.add(action);

		String title = action.getTitle();

		View container = (View) inflater.inflate(R.layout.action_item, null);
		container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		final int pos = mChildPos;
		final int iconId = action.getIconId();

		TextView text = (TextView) container.findViewById(R.id.tv_title);
		text.setText(title);
		text.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);

		container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItemClickListener != null) {
					mItemClickListener.onItemClick(QuickAction.this, pos,
							iconId);
				}

				if (!getActionItem(pos).isSticky()) {
					mDidAction = true;

					// workaround for transparent background bug
					// thx to Roman Wozniak <roman.wozniak@gmail.com>
					v.post(new Runnable() {
						@Override
						public void run() {
							dismiss();
						}
					});
				}
			}
		});

		mTrack.addView(container, mChildPos);

		mChildPos++;
	}

	public void setOnActionItemClickListener(OnActionItemClickListener listener) {
		mItemClickListener = listener;
	}

	/**
	 * Show popup mWindow
	 */
	@SuppressWarnings("deprecation")
	public void show(View anchor) {
		preShow();

		int[] location = new int[2];

		mDidAction = false;

		anchor.getLocationOnScreen(location);

		Rect anchorRect = new Rect(location[0], location[1], location[0]
				+ anchor.getWidth(), location[1] + anchor.getHeight());

		// mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT));
		mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		int rootWidth = mRootView.getMeasuredWidth();
		int rootHeight = mRootView.getMeasuredHeight();

		int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
		// int screenHeight = mWindowManager.getDefaultDisplay().getHeight();

		int xPos = (screenWidth - rootWidth) / 2;
		int yPos = anchorRect.top - rootHeight;

		// display on bottom
		if (rootHeight > anchor.getTop()) {
			yPos = anchorRect.bottom;
		}

		showArrow();

		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);

	}

	/**
	 * Show arrow
	 */
	private void showArrow() {

		final int arrowWidth = mArrowUp.getMeasuredWidth();

		ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) mArrowUp
				.getLayoutParams();

		param.leftMargin = -arrowWidth / 2;

	}

	/**
	 * Set listener for window dismissed. This listener will only be fired if
	 * the quicakction dialog is dismissed by clicking outside the dialog or
	 * clicking on sticky item.
	 */
	public void setOnDismissListener(QuickAction.OnDismissListener listener) {
		setOnDismissListener(this);

		mDismissListener = listener;
	}

	@Override
	public void onDismiss() {
		if (!mDidAction && mDismissListener != null) {
			mDismissListener.onDismiss();
		}
	}

	/**
	 * Listener for item click
	 * 
	 */
	public interface OnActionItemClickListener {
		public abstract void onItemClick(QuickAction source, int pos, int iconId);
	}

	/**
	 * Listener for window dismiss
	 * 
	 */
	public interface OnDismissListener {
		public abstract void onDismiss();
	}
}