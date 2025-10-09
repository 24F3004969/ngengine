package androidx.fragment.app;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
public class Fragment {
    public  void onCreate(Bundle savedInstanceState){

    }
    public void onAttach(Context context) {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
    }

    public void onStart() {
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onStop() {
    }

    public void onDestroyView() {
    }

    public void onDestroy() {
    }

    public FragmentActivity requireActivity() {
        return null;
    }

    public Context getContext() {
        return null;
    }
}
