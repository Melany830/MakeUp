package ru.flightlabs.makeup.shader;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import org.opencv.core.Point;

import ru.flightlabs.makeup.StateEditor;
import ru.flightlabs.makeup.activity.ActivityMakeUp;
import ru.flightlabs.makeup.utils.ModelUtils;
import ru.flightlabs.masks.renderer.ShaderEffect;
import ru.flightlabs.masks.renderer.ShaderEffectHelper;
import ru.flightlabs.masks.utils.OpenGlHelper;
import ru.flightlabs.masks.utils.PointsConverter;
import ru.flightlabs.masks.utils.PoseHelper;

/**
 * Created by sov on 13.02.2017.
 */
public class ShaderEffectMakeUp extends ShaderEffect {
    private static final String TAG = "ShaderEffectMakeUp";

    private int eyeShadowTextureId;
    private int eyeLashesTextureId;
    private int eyeLineTextureId;
    private int lipsTextureId;

    private final StateEditor editEnv;

    public ShaderEffectMakeUp(Context contex, StateEditor editEnv) {
        super(contex);
        this.editEnv = editEnv;
    }

    public void init() {
        super.init();
        eyeShadowTextureId = OpenGlHelper.loadTexture(context, editEnv.getResourceId(StateEditor.EYE_SHADOW));
        eyeLashesTextureId = OpenGlHelper.loadTexture(context, editEnv.getResourceId(StateEditor.EYE_LASH));
        eyeLineTextureId = OpenGlHelper.loadTexture(context, editEnv.getResourceId(StateEditor.EYE_LINE));
        lipsTextureId = OpenGlHelper.loadTexture(context, editEnv.getResourceId(StateEditor.LIPS));
    }

    public void makeShaderMask(int indexEye, PoseHelper.PoseResult poseResult, int width, int height, int texIn, long time, int iGlobTime) {
        Log.i(TAG, "onDrawFrame6 draw maekup");
        int vPos2 = GLES20.glGetAttribLocation(program2dJustCopy, "vPosition");
        int vTex2 = GLES20.glGetAttribLocation(program2dJustCopy, "vTexCoord");
        GLES20.glEnableVertexAttribArray(vPos2);
        GLES20.glEnableVertexAttribArray(vTex2);
        ShaderEffectHelper.shaderEffect2dWholeScreen(poseResult.leftEye, poseResult.rightEye, texIn, program2dJustCopy, vPos2, vTex2);

        if (poseResult.foundLandmarks != null) {
            Point[] onImageEyeLeft = ModelUtils.getOnlyPoints(poseResult.foundLandmarks, 36, 6);
            Point[] onImageEyeRight = ModelUtils.getOnlyPoints(poseResult.foundLandmarks, 36 + 6, 6);
            // TODO add checkbox for rgb or hsv bleding
            Log.i(TAG, "onDrawFrame6 draw maekup2");
            int vPos22 = GLES20.glGetAttribLocation(program2dTriangles, "vPosition");
            int vTex22 = GLES20.glGetAttribLocation(program2dTriangles, "vTexCoord");
            GLES20.glEnableVertexAttribArray(vPos22);
            GLES20.glEnableVertexAttribArray(vTex22);
            // TODO use blendshape for eyes

            if (editEnv.changed(StateEditor.EYE_SHADOW)) {
                OpenGlHelper.changeTexture(context, editEnv.getResourceId(StateEditor.EYE_SHADOW), eyeShadowTextureId);
            }
            if (editEnv.changed(StateEditor.EYE_LASH)) {
                OpenGlHelper.changeTexture(context, editEnv.getResourceId(StateEditor.EYE_LASH), eyeLashesTextureId);
            }
            if (editEnv.changed(StateEditor.EYE_LINE)) {
                OpenGlHelper.changeTexture(context, editEnv.getResourceId(StateEditor.EYE_LINE), eyeLineTextureId);
            }
            if (editEnv.changed(StateEditor.LIPS)) {
                OpenGlHelper.changeTexture(context, editEnv.getResourceId(StateEditor.LIPS), lipsTextureId);
            }
            Point[] onImage = PointsConverter.completePointsByAffine(onImageEyeLeft, PointsConverter.convertToOpencvPoints(StateEditor.pointsLeftEye), new int[]{0, 1, 2, 3, 4, 5});
            // TODO use blendshapes
            onImage = PointsConverter.replacePoints(onImage, onImageEyeLeft, new int[]{0, 1, 2, 3, 4, 5});
            ShaderEffectHelper.effect2dTriangles(program2dTriangles, texIn, eyeShadowTextureId, PointsConverter.convertFromPointsGlCoord(onImage, width, height), PointsConverter.convertFromPointsGlCoord(StateEditor.pointsLeftEye, 512, 512), vPos22, vTex22, PointsConverter.convertTriangle(StateEditor.trianglesLeftEye), eyeLashesTextureId, eyeLineTextureId,
                    new int[] {ActivityMakeUp.useHsvOrColorized? 2 : 1, 0, 0},
                    PointsConverter.convertTovec3(editEnv.getColor(StateEditor.EYE_SHADOW)),
                    PointsConverter.convertTovec3(editEnv.getColor(StateEditor.EYE_LASH)),
                    PointsConverter.convertTovec3(editEnv.getColor(StateEditor.EYE_LINE)),
                    editEnv.getOpacityFloat(StateEditor.EYE_SHADOW), editEnv.getOpacityFloat(StateEditor.EYE_LASH), editEnv.getOpacityFloat(StateEditor.EYE_LINE));

            Point[] onImageRight = PointsConverter.completePointsByAffine(PointsConverter.reallocateAndCut(onImageEyeRight, new int[] {3, 2, 1, 0 , 5, 4}), PointsConverter.convertToOpencvPoints(StateEditor.pointsLeftEye), new int[]{0, 1, 2, 3, 4, 5});
            //onImageRight = PointsConverter.replacePoints(onImageRight, onImageEyeRight, new int[]{3, 2, 1, 0 , 5, 4});
            // FIXME flip triangle on right eyes, cause left and right triangles are not the same
            ShaderEffectHelper.effect2dTriangles(program2dTriangles, texIn, eyeShadowTextureId, PointsConverter.convertFromPointsGlCoord(onImageRight, width, height), PointsConverter.convertFromPointsGlCoord(StateEditor.pointsLeftEye, 512, 512), vPos22, vTex22, PointsConverter.convertTriangle(StateEditor.trianglesLeftEye), eyeLashesTextureId, eyeLineTextureId,
                    new int[] {ActivityMakeUp.useHsvOrColorized? 2 : 1, 0, 0},
                    PointsConverter.convertTovec3(editEnv.getColor(StateEditor.EYE_SHADOW)),
                    PointsConverter.convertTovec3(editEnv.getColor(StateEditor.EYE_LASH)),
                    PointsConverter.convertTovec3(editEnv.getColor(StateEditor.EYE_LINE)),
                    editEnv.getOpacityFloat(StateEditor.EYE_SHADOW), editEnv.getOpacityFloat(StateEditor.EYE_LASH), editEnv.getOpacityFloat(StateEditor.EYE_LINE));

            Point[] onImageLips = ModelUtils.getOnlyPoints(poseResult.foundLandmarks, 48, 20);
            Point[] onImageLipsConv = PointsConverter.completePointsByAffine(onImageLips, PointsConverter.convertToOpencvPoints(StateEditor.pointsWasLips), new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
            onImageLipsConv = PointsConverter.replacePoints(onImageLipsConv, onImageLips, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
            ShaderEffectHelper.effect2dTriangles(program2dTriangles, texIn, lipsTextureId, PointsConverter.convertFromPointsGlCoord(onImageLipsConv, width, height), PointsConverter.convertFromPointsGlCoord(StateEditor.pointsWasLips, 512, 512), vPos22, vTex22, PointsConverter.convertTriangle(StateEditor.trianglesLips), lipsTextureId, lipsTextureId,
                    new int[] {ActivityMakeUp.useHsvOrColorized? 2 : 1, -1, -1},
                    PointsConverter.convertTovec3(editEnv.getColor(StateEditor.LIPS)), null, null,
                    editEnv.getOpacityFloat(StateEditor.LIPS), 0, 0);

            // FIXME elements erase each other
        }
    }
}