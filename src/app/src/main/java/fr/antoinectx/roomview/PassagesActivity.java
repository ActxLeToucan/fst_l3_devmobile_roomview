package fr.antoinectx.roomview;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.antoinectx.roomview.models.Area;
import fr.antoinectx.roomview.models.DirectionPhoto;
import fr.antoinectx.roomview.models.Passage;

/**
 * Listens to user selection of the other side of a passage.
 */
interface SelectOtherSide_OnSelectListener {
    /**
     * Called when the user selects the other side of a passage
     *
     * @param otherSide The other side of the passage
     */
    void onSelect(Area otherSide);
}

/**
 * Action performed just before dismissing the dialog to select the other side of a passage
 */
interface SelectOtherSide_BeforeDismissListener {
    /**
     * Called just before dismissing the dialog to select the other side of a passage
     */
    void beforeDismiss();
}

public class PassagesActivity extends PassageViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passages);

        initAppBar(direction.getName(this), "", true);

        imageView = findViewById(R.id.passagesView_imageView);
        File photo = area.getFile(this, direction);
        if (photo == null || !photo.exists()) {
            Toast.makeText(this, R.string.no_photo, Toast.LENGTH_SHORT).show();
            finish();
        }
        applyImage(photo, new OnTouchListner() {

            @Override
            public void onSelection(SurfaceHolder surfaceHolder, Context parent, double[] imageSelection, double[] screenSelection) {
                double x1Rel = imageSelection[0];
                double y1Rel = imageSelection[1];
                double x2Rel = imageSelection[2];
                double y2Rel = imageSelection[3];
                boolean isValidSelection = !(x1Rel == x2Rel || y1Rel == y2Rel);

                // selection sur l'ecran
                Rect rect = new Rect();
                rect.left = (int) screenSelection[0];
                rect.top = (int) screenSelection[1];
                rect.right = (int) screenSelection[2];
                rect.bottom = (int) screenSelection[3];
                rect.sort();


                Paint paint = new Paint();
                paint.setColor(isValidSelection ? Color.GREEN : Color.RED);
                paint.setAlpha(100);
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.drawRect(rect, paint);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void afterSelection(SurfaceHolder surfaceHolder, Context parent, double[] imageSelection, SimpleClickEnabler clickEnabler) {
                double x1 = imageSelection[0];
                double y1 = imageSelection[1];
                double x2 = imageSelection[2];
                double y2 = imageSelection[3];
                boolean isValidSelection = !(x1 == x2 || y1 == y2);

                if (isValidSelection) {
                    selectOtherSide(surfaceHolder,
                            (otherSide) -> {
                                Passage passage = new Passage(x1, y1, x2, y2, otherSide);
                                DirectionPhoto directionPhoto = area.getDirectionPhoto(direction);
                                directionPhoto.getPassages().add(passage);
                            },
                            clickEnabler::enable
                    );
                } else {
                    new AlertDialog.Builder(parent)
                            .setTitle(R.string.passagesActivity_invalidSelection_title)
                            .setMessage(R.string.passagesActivity_invalidSelection_message)
                            .setPositiveButton("OK", (dialogInterface, i) -> {
                                Log.d("PassagesActivity", "OK");
                                clickEnabler.enable();
                            })
                            .setOnCancelListener(dialogInterface -> {
                                Log.d("PassagesActivity", "Cancel");
                                clickEnabler.enable();
                            })
                            .setOnDismissListener(dialogInterface -> {
                                Log.d("PassagesActivity", "Dismiss");
                                clickEnabler.enable();
                            })
                            .show();

                    draw(surfaceHolder);
                }
            }

            @Override
            public void onPassageClick(SurfaceHolder surfaceHolder, Context parent, Passage passage) {
                editPassage(surfaceHolder, passage);
            }
        });
    }

    /**
     * Show a dialog to edit a passage
     *
     * @param surfaceHolder The surface holder to draw on
     * @param passage       The passage to edit
     */
    private void editPassage(SurfaceHolder surfaceHolder, Passage passage) {
        List<Passage> selectedPassage = new ArrayList<>();
        selectedPassage.add(passage);

        draw(surfaceHolder, selectedPassage, Color.YELLOW);

        CharSequence[] items = new CharSequence[]{
                getString(R.string.action_edit),
                getString(R.string.action_delete),
                getString(R.string.action_cancel)
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.passagesActivity_selectAction_title)
                .setItems(items, (dialog, item) -> {
                    if (item == 0) {
                        selectOtherSide(surfaceHolder,
                                (otherSide) -> {
                                    passage.setOtherSide(otherSide);
                                    DirectionPhoto directionPhoto = area.getDirectionPhoto(direction);
                                    directionPhoto.getPassages().removeIf(p -> p.getId().equals(passage.getId()));
                                    directionPhoto.getPassages().add(new Passage(passage.getX1(), passage.getY1(), passage.getX2(), passage.getY2(), otherSide));
                                },
                                null
                        );
                        dialog.dismiss();
                    } else if (item == 1) {
                        deletePassage(surfaceHolder, passage);
                        dialog.dismiss();
                    } else {
                        draw(surfaceHolder);
                    }
                })
                .setOnCancelListener(dialogInterface -> draw(surfaceHolder))
                .show();
    }

    /**
     * Show a dialog to select the other side of the passage
     *
     * @param surfaceHolder         The surface holder to draw on
     * @param onSelectListener      The listener to call when the other side is selected
     * @param beforeDismissListener The listener to call before dismissing the dialog
     */
    private void selectOtherSide(SurfaceHolder surfaceHolder, SelectOtherSide_OnSelectListener onSelectListener, @Nullable SelectOtherSide_BeforeDismissListener beforeDismissListener) {
        List<Area> areas = building.getAreas().stream()
                .filter(a -> !a.getId().equals(area.getId()))
                .collect(Collectors.toList());
        ArrayAdapter<Area> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, areas);

        View layout = getLayoutInflater().inflate(R.layout.dialog_create_passage, null);
        MaterialAutoCompleteTextView autoCompleteTextView = layout.findViewById(R.id.passage_field_area);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setOnCancelListener(dialogInterface -> {
            draw(surfaceHolder);
            if (beforeDismissListener != null) {
                beforeDismissListener.beforeDismiss();
            }
        });
        AlertDialog dialog = builder.create();


        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
            Area selectedArea = (Area) parent.getItemAtPosition(position);
            if (selectedArea != null) {
                onSelectListener.onSelect(selectedArea);
            } else {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            }

            savePassages();
            draw(surfaceHolder);

            if (beforeDismissListener != null) {
                beforeDismissListener.beforeDismiss();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Delete a passage
     *
     * @param surfaceHolder The surface holder to draw on
     * @param passage       The passage to delete
     */
    private void deletePassage(SurfaceHolder surfaceHolder, Passage passage) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setTitle(R.string.warning)
                .setMessage(R.string.passagesActivity_deletePassage_message)
                .setPositiveButton(R.string.action_delete, (dialogInterface, i) -> {
                    DirectionPhoto directionPhoto = area.getDirectionPhoto(direction);
                    directionPhoto.getPassages().removeIf(p -> p.getId().equals(passage.getId()));
                    savePassages();
                    draw(surfaceHolder);
                })
                .setNegativeButton(R.string.action_cancel, (dialogInterface, i) -> {
                    draw(surfaceHolder);
                })
                .setOnCancelListener(dialogInterface -> {
                    draw(surfaceHolder);
                })
                .show();
    }

    private void savePassages() {
        boolean found = false;
        for (int i = 0; i < building.getAreas().size(); i++) {
            if (building.getAreas().get(i).getId().equals(area.getId())) {
                building.getAreas().set(i, area);
                found = true;
                break;
            }
        }
        if (!found) building.getAreas().add(area);
        building.save(this);
    }
}