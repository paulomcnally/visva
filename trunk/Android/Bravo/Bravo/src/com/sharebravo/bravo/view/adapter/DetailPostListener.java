package com.sharebravo.bravo.view.adapter;

public interface DetailPostListener {
    public void goToCallSpot();

    public void goToFragment(int framentID);

    public void goToShare();

    public void goToSave(boolean isSave);
    public void goToLike(boolean isLike);

    public void goToSubmitComment(String commentText);

    public void goToReport();

    public void goToFollow(boolean isFollow);

    public void goToUserDataTab(String useId);

    public void goToCoverImage();

    public void goToLiked();

    public void goToSaved();

    public void choosePicture();

}
