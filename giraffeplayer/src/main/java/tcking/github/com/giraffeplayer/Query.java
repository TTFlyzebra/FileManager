package tcking.github.com.giraffeplayer;

import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * Created by 李冰锋 on 2016/12/14 11:27.
 * E-mail:libf@ppfuns.com
 * Package: tcking.github.com.giraffeplayer
 */
public class Query {
    private WeakReference<Activity> mActivityWeakReference;
    private View view;
    private SparseArray<View> viewList;

    public Query(Activity activity) {
        mActivityWeakReference = new WeakReference<Activity>(activity);
        viewList = new SparseArray<>();
    }

    public Query id(int id) {
        this.view = viewList.get(id);
        if (this.view == null && mActivityWeakReference.get() != null) {
            this.view = mActivityWeakReference.get().findViewById(id);
            viewList.put(id, view);
        }
        return this;
    }

    public Query image(int resId) {
        if (view instanceof ImageView) {
            ((ImageView) view).setImageResource(resId);
        }
        return this;
    }

    public Query visible() {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public Query gone() {
        if (view != null) {
            view.setVisibility(View.GONE);
        }
        return this;
    }

    public Query clicked(View.OnClickListener handler) {
        if (view != null) {
            view.setOnClickListener(handler);
        }
        return this;
    }

    public Query text(CharSequence text) {
        if (view != null && view instanceof TextView) {
            ((TextView) view).setText(text);
        }
        return this;
    }

    public Query visibility(int visible) {
        if (view != null) {
            view.setVisibility(visible);
        }
        return this;
    }

    private void size(boolean width, int n, boolean dip) {

        if (view != null) {

            ViewGroup.LayoutParams lp = view.getLayoutParams();


            if (n > 0 && dip) {
                n = dip2pixel(mActivityWeakReference.get(), n);
            }

            if (width) {
                lp.width = n;
            } else {
                lp.height = n;
            }

            view.setLayoutParams(lp);

        }

    }

    public View view() {
        return view;
    }

    public void height(int height, boolean dip) {
        size(false, height, dip);
    }

    public int dip2pixel(Context context, float n) {
        int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n, context.getResources().getDisplayMetrics());
        return value;
    }

}
