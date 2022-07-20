package com.hrfuelwidget;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.hrfuelwidget.databinding.DialogFuelSliderBinding;

public class HRFuelSliderDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    private Context context;
    private DialogFuelSliderBinding binding;
    private FuelReadingCallBack callBack;
    private int fuelReading=0;


    public static HRFuelSliderDialog newInstance(FuelReadingCallBack callBack) {
        return new HRFuelSliderDialog(callBack);
    }

    private HRFuelSliderDialog(FuelReadingCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.BottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_fuel_slider, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
        binding.viewBtnConnect.setOnClickListener(this);
        binding.viewClose.setOnClickListener(this);
        setCancelable(false);

        float value = Float.parseFloat(String.valueOf(fuelReading)) / 100f;
        binding.fuelView.setFuelLevel(value);
        binding.seekBar.setProgress(Math.round(value * 100));
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    handleSeekBarChanged(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    protected void handleSeekBarChanged(int progress) {
        float newLevel = (float) progress / 100f;
        Log.d("fuel Reading ", "========> " + newLevel);
        binding.fuelView.setFuelLevel(newLevel);
        fuelReading = progress;
        if (callBack != null) {
            binding.viewSearching.setText("Fuel percentage:"+ fuelReading+"%");
            callBack.onFuelReading(fuelReading);
        }
    }

    private void dismissDialog() {
        if (!((Activity) context).isFinishing()) {
            if (callBack!=null) callBack.onFuelReading(fuelReading);
            dismiss();
        }
    }

    public interface FuelReadingCallBack {
        void onFuelReading(int reading);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.viewBtnConnect) {
            if (callBack != null) {
                callBack.onFuelReading(fuelReading);
                dismissDialog();
            }
        } else if (id == R.id.viewClose) {
            dismissDialog();
        }
    }


}
