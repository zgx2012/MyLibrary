package cm.android.common.am.ui;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Comparator;

import cm.android.sdk.widget.adapter.MyBaseAdapter;

public abstract class BaseAppAdapter extends MyBaseAdapter<ApplicationsState.AppEntry> implements
        Filterable, ApplicationsState.Callbacks {

    //ggg
    public static final int SIZE_TOTAL = 0;

    public static final int SIZE_INTERNAL = 1;

    public static final int SIZE_EXTERNAL = 2;

    private static final int MENU_OPTIONS_BASE = 0;

    public static final int FILTER_APPS_ALL = MENU_OPTIONS_BASE + 0;

    public static final int FILTER_APPS_THIRD_PARTY = MENU_OPTIONS_BASE + 1;

    public static final int FILTER_APPS_SDCARD = MENU_OPTIONS_BASE + 2;

    public static final int FILTER_APPS_DISABLED = MENU_OPTIONS_BASE + 3;

    public static final int SORT_ORDER_ALPHA = MENU_OPTIONS_BASE + 4;

    public static final int SORT_ORDER_SIZE = MENU_OPTIONS_BASE + 5;

    public static final int SHOW_RUNNING_SERVICES = MENU_OPTIONS_BASE + 6;

    public static final int SHOW_BACKGROUND_PROCESSES = MENU_OPTIONS_BASE + 7;

    public static final int RESET_APP_PREFERENCES = MENU_OPTIONS_BASE + 8;

    public static final int FILTER_APPS_THIRD_PARTY_EXCLUDE_SELF = MENU_OPTIONS_BASE + 100;

    private final ApplicationsState mState;

    private final ApplicationsState.Session mSession;

    private final ArrayList<View> mActive = new ArrayList<View>();

    private final int mFilterMode;

    private ArrayList<ApplicationsState.AppEntry> mBaseEntries;

//    private ArrayList<ApplicationsState.AppEntry> mEntries;

    private boolean mResumed;

    private int mLastSortMode = -1;

    private boolean mWaitingForData;

    private int mWhichSize = SIZE_TOTAL;

    CharSequence mCurFilterPrefix;

    private Filter mFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<ApplicationsState.AppEntry> entries
                    = applyPrefixFilter(constraint, mBaseEntries);
            FilterResults fr = new FilterResults();
            fr.values = entries;
            fr.count = entries.size();
            return fr;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mCurFilterPrefix = constraint;
//            mEntries = (ArrayList<ApplicationsState.AppEntry>) results.values;
            update((ArrayList<ApplicationsState.AppEntry>) results.values);
            notifyDataSetChanged();
        }
    };

    public BaseAppAdapter(Context context, ApplicationsState state, int filterMode) {
        super(context);
        mState = state;
        mSession = state.newSession(this);
        mFilterMode = filterMode;
    }

    public void resume(int sort) {
        if (!mResumed) {
            mResumed = true;
            mSession.resume();
            mLastSortMode = sort;
            rebuild(true);
        } else {
            rebuild(sort);
        }
    }

    public void pause() {
        if (mResumed) {
            mResumed = false;
            mSession.pause();
        }
    }

    public void release() {
        mSession.release();
    }

    public void rebuild(int sort) {
        if (sort == mLastSortMode) {
            return;
        }
        mLastSortMode = sort;
        rebuild(true);
    }

    public void rebuild(boolean eraseold) {
        ApplicationsState.AppFilter filterObj;
        Comparator<ApplicationsState.AppEntry> comparatorObj;
        boolean emulated = Environment.isExternalStorageEmulated();
        if (emulated) {
            mWhichSize = SIZE_TOTAL;
        } else {
            mWhichSize = SIZE_INTERNAL;
        }
        switch (mFilterMode) {
            case FILTER_APPS_THIRD_PARTY:
                filterObj = ApplicationsState.THIRD_PARTY_FILTER;
                break;
            case FILTER_APPS_SDCARD:
                filterObj = ApplicationsState.ON_SD_CARD_FILTER;
                if (!emulated) {
                    mWhichSize = SIZE_EXTERNAL;
                }
                break;
            case FILTER_APPS_DISABLED:
                filterObj = ApplicationsState.DISABLED_FILTER;
                break;
            default:
                filterObj = ApplicationsState.ALL_ENABLED_FILTER;
                break;
        }
        switch (mLastSortMode) {
            case SORT_ORDER_SIZE:
                switch (mWhichSize) {
                    case SIZE_INTERNAL:
                        comparatorObj = ApplicationsState.INTERNAL_SIZE_COMPARATOR;
                        break;
                    case SIZE_EXTERNAL:
                        comparatorObj = ApplicationsState.EXTERNAL_SIZE_COMPARATOR;
                        break;
                    default:
                        comparatorObj = ApplicationsState.SIZE_COMPARATOR;
                        break;
                }
                break;
            default:
                comparatorObj = ApplicationsState.ALPHA_COMPARATOR;
                break;
        }
        ArrayList<ApplicationsState.AppEntry> entries
                = mSession.rebuild(filterObj, comparatorObj);
        if (entries == null && !eraseold) {
            // Don't have new list yet, but can continue using the old one.
            return;
        }
        mBaseEntries = entries;
        if (mBaseEntries != null) {
            //ggg
//            mEntries = applyPrefixFilter(mCurFilterPrefix, mBaseEntries);
            update(applyPrefixFilter(mCurFilterPrefix, mBaseEntries));
        } else {
            clear();
//            mEntries = null;
        }
        notifyDataSetChanged();
//        mTab.updateStorageUsage();

        if (entries == null) {
            mWaitingForData = true;
//            mTab.mListContainer.setVisibility(View.INVISIBLE);
//            mTab.mLoadingContainer.setVisibility(View.VISIBLE);
        } else {
//            mTab.mListContainer.setVisibility(View.VISIBLE);
//            mTab.mLoadingContainer.setVisibility(View.GONE);
        }
    }

    ArrayList<ApplicationsState.AppEntry> applyPrefixFilter(CharSequence prefix,
            ArrayList<ApplicationsState.AppEntry> origEntries) {
        if (prefix == null || prefix.length() == 0) {
            return origEntries;
        } else {
            String prefixStr = ApplicationsState.normalize(prefix.toString());
            final String spacePrefixStr = " " + prefixStr;
            ArrayList<ApplicationsState.AppEntry> newEntries
                    = new ArrayList<ApplicationsState.AppEntry>();
            for (int i = 0; i < origEntries.size(); i++) {
                ApplicationsState.AppEntry entry = origEntries.get(i);
                String nlabel = entry.getNormalizedLabel();
                if (nlabel.startsWith(prefixStr) || nlabel.indexOf(spacePrefixStr) != -1) {
                    newEntries.add(entry);
                }
            }
            return newEntries;
        }
    }

    @Override
    public void onRunningStateChanged(boolean running) {
//        mTab.mOwner.getActivity().setProgressBarIndeterminateVisibility(running);
    }

    @Override
    public void onRebuildComplete(ArrayList<ApplicationsState.AppEntry> apps) {
//        if (mTab.mLoadingContainer.getVisibility() == View.VISIBLE) {
//            mTab.mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(
//                    mContext, android.R.anim.fade_out));
//            mTab.mListContainer.startAnimation(AnimationUtils.loadAnimation(
//                    mContext, android.R.anim.fade_in));
//        }
//        mTab.mListContainer.setVisibility(View.VISIBLE);
//        mTab.mLoadingContainer.setVisibility(View.GONE);
        mWaitingForData = false;
        mBaseEntries = apps;
        //ggg
//        mEntries = applyPrefixFilter(mCurFilterPrefix, mBaseEntries);
        update(applyPrefixFilter(mCurFilterPrefix, mBaseEntries));
        notifyDataSetChanged();
//        mTab.updateStorageUsage();
    }

    @Override
    public void onPackageListChanged() {
        rebuild(false);
    }

    @Override
    public void onPackageIconChanged() {
        // We ensure icons are loaded when their item is displayed, so
        // don't care about icons loaded in the background.
    }

    @Override
    public void onPackageSizeChanged(String packageName) {
//        for (int i = 0; i < mActive.size(); i++) {
//            AppViewHolder holder = (AppViewHolder) mActive.get(i).getTag();
//            if (holder.entry.info.packageName.equals(packageName)) {
//                synchronized (holder.entry) {
//                    holder.updateSizeText(mTab.mInvalidSizeStr, mWhichSize);
//                }
//                if (holder.entry.info.packageName.equals(mTab.mOwner.mCurrentPkgName)
//                        && mLastSortMode == SORT_ORDER_SIZE) {
//                    // We got the size information for the last app the
//                    // user viewed, and are sorting by size...  they may
//                    // have cleared data, so we immediately want to resort
//                    // the list with the new size to reflect it to the user.
//                    rebuild(false);
//                }
//                mTab.updateStorageUsage();
//                return;
//            }
//        }
    }

    @Override
    public void onAllSizesComputed() {
        if (mLastSortMode == SORT_ORDER_SIZE) {
            rebuild(false);
        }
//        mTab.updateStorageUsage();
    }

//    public int getCount() {
//        return mEntries != null ? mEntries.size() : 0;
//    }
//
//    public Object getItem(int position) {
//        return mEntries.get(position);
//    }

    public ApplicationsState.AppEntry getAppEntry(int position) {
//ggg
//        return mEntries.get(position);
        return getItem(position);
    }

    public long getItemId(int position) {
        //ggg
//        return mEntries.get(position).id;
        return getItem(position).id;
    }

//    public View getView(int position, View convertView, ViewGroup parent) {
//        // A ViewHolder keeps references to children views to avoid unnecessary calls
//        // to findViewById() on each row.
//        AppViewHolder holder = AppViewHolder.createOrRecycle(mTab.mInflater, convertView);
//        convertView = holder.rootView;
//
//        // Bind the data efficiently with the holder
//        ApplicationsState.AppEntry entry = mEntries.get(position);
//        synchronized (entry) {
//            holder.entry = entry;
//            if (entry.label != null) {
//                holder.appName.setText(entry.label);
//            }
//            mState.ensureIcon(entry);
//            if (entry.icon != null) {
//                holder.appIcon.setImageDrawable(entry.icon);
//            }
//            holder.updateSizeText(mTab.mInvalidSizeStr, mWhichSize);
//            if ((entry.info.flags & ApplicationInfo.FLAG_INSTALLED) == 0) {
//                holder.disabled.setVisibility(View.VISIBLE);
//                holder.disabled.setText(R.string.not_installed);
//            } else if (!entry.info.enabled) {
//                holder.disabled.setVisibility(View.VISIBLE);
//                holder.disabled.setText(R.string.disabled);
//            } else {
//                holder.disabled.setVisibility(View.GONE);
//            }
//            if (mFilterMode == FILTER_APPS_SDCARD) {
//                holder.checkBox.setVisibility(View.VISIBLE);
//                holder.checkBox.setChecked((entry.info.flags
//                        & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0);
//            } else {
//                holder.checkBox.setVisibility(View.GONE);
//            }
//        }
//        mActive.remove(convertView);
//        mActive.add(convertView);
//        return convertView;
//    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public void onMovedToScrapHeap(View view) {
        mActive.remove(view);
    }
}
