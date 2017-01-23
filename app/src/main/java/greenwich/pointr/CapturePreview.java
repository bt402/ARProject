package greenwich.pointr;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;

/*
 * To implement Augmented Reality use this class for accessing the camera
 * The camera will be overlayed over the MainActivity
 */

public class CapturePreview extends SurfaceView implements SurfaceHolder.Callback{

    SurfaceHolder holder;
    static Camera mCamera;

    public CapturePreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.getSupportedPreviewSizes();
        // Phone rotated to portrait, by 90 degrees to left
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            mCamera.setDisplayOrientation(90);
        }
        mCamera.startPreview();
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            mCamera = Camera.open();
            mCamera.setPreviewDisplay(holder);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }
}
