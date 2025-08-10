package com.media.player.service.databinding;
import com.media.player.service.R;
import com.media.player.service.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivityMainBindingImpl extends ActivityMainBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.btnBack, 1);
        sViewsWithIds.put(R.id.tv_service_status, 2);
        sViewsWithIds.put(R.id.btn_toggle_service, 3);
        sViewsWithIds.put(R.id.switch_call_mode, 4);
        sViewsWithIds.put(R.id.tv_call_mode_status, 5);
        sViewsWithIds.put(R.id.card_distance_settings, 6);
        sViewsWithIds.put(R.id.tv_current_distance, 7);
        sViewsWithIds.put(R.id.chip_group_distance, 8);
        sViewsWithIds.put(R.id.card_filter_settings, 9);
        sViewsWithIds.put(R.id.edit_keywords, 10);
        sViewsWithIds.put(R.id.btn_destination_settings, 11);
        sViewsWithIds.put(R.id.btn_exclusion_settings, 12);
        sViewsWithIds.put(R.id.switch_auto_deny, 13);
        sViewsWithIds.put(R.id.switch_volume_control, 14);
        sViewsWithIds.put(R.id.btn_save_template, 15);
        sViewsWithIds.put(R.id.btn_load_template, 16);
    }
    // views
    @NonNull
    private final android.widget.LinearLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityMainBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 17, sIncludes, sViewsWithIds));
    }
    private ActivityMainBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.ImageView) bindings[1]
            , (com.google.android.material.button.MaterialButton) bindings[11]
            , (com.google.android.material.button.MaterialButton) bindings[12]
            , (com.google.android.material.button.MaterialButton) bindings[16]
            , (com.google.android.material.button.MaterialButton) bindings[15]
            , (com.google.android.material.button.MaterialButton) bindings[3]
            , (com.google.android.material.card.MaterialCardView) bindings[6]
            , (com.google.android.material.card.MaterialCardView) bindings[9]
            , (com.google.android.material.chip.ChipGroup) bindings[8]
            , (com.google.android.material.textfield.TextInputEditText) bindings[10]
            , (com.google.android.material.switchmaterial.SwitchMaterial) bindings[13]
            , (com.google.android.material.switchmaterial.SwitchMaterial) bindings[4]
            , (com.google.android.material.switchmaterial.SwitchMaterial) bindings[14]
            , (android.widget.TextView) bindings[5]
            , (android.widget.TextView) bindings[7]
            , (android.widget.TextView) bindings[2]
            );
        this.mboundView0 = (android.widget.LinearLayout) bindings[0];
        this.mboundView0.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x1L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
            return variableSet;
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        // batch finished
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): null
    flag mapping end*/
    //end
}