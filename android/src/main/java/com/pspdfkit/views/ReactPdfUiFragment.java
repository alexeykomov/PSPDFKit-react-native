/*
 * ReactPdfUiFragment.java
 *
 *   PSPDFKit
 *
 *   Copyright © 2021-2023 PSPDFKit GmbH. All rights reserved.
 *
 *   THIS SOURCE CODE AND ANY ACCOMPANYING DOCUMENTATION ARE PROTECTED BY INTERNATIONAL COPYRIGHT LAW
 *   AND MAY NOT BE RESOLD OR REDISTRIBUTED. USAGE IS BOUND TO THE PSPDFKIT LICENSE AGREEMENT.
 *   UNAUTHORIZED REPRODUCTION OR DISTRIBUTION IS SUBJECT TO CIVIL AND CRIMINAL PENALTIES.
 *   This notice may not be removed from this file.
 */

package com.pspdfkit.views;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.facebook.react.uimanager.events.EventDispatcher;
import com.pspdfkit.document.PdfDocument;
import com.pspdfkit.react.R;
import com.pspdfkit.ui.PdfActivity;
import com.pspdfkit.ui.PdfFragment;
import com.pspdfkit.ui.PdfUiFragment;

import java.util.List;
import java.util.Objects;

/**
 * This {@link PdfUiFragment} provides additional callbacks to improve integration into react native.
 * <p/>
 * <ul>
 * <li>A callback when the configuration was changed.</li>
 * <li>A method to show and hide the navigation button in the toolbar, as well as a callback for when it is clicked.</li>
 * </ul>
 */
public class ReactPdfUiFragment extends PdfUiFragment {

  @Nullable
  private ReactPdfUiFragmentListener reactPdfUiFragmentListener;

  @Nullable
  private EventDispatcher eventDispatcher;
  private int viewId;

  private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
    @Override
    public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
      super.onFragmentCreated(fm, f, savedInstanceState);
      // Whenever a new PdfFragment is created that means the configuration has changed.
      if (f instanceof PdfFragment) {
        if (reactPdfUiFragmentListener != null) {
          reactPdfUiFragmentListener.onConfigurationChanged(ReactPdfUiFragment.this);
        }
      }
    }
  };

  void setReactPdfUiFragmentListener(@Nullable ReactPdfUiFragmentListener listener) {
    this.reactPdfUiFragmentListener = listener;
  }

  /**
   * When set to true will add a navigation arrow to the toolbar.
   */
  void setShowNavigationButtonInToolbar(final boolean showNavigationButtonInToolbar) {
    if (getView() == null) {
      return;
    }
    Toolbar toolbar = getView().findViewById(R.id.pspdf__toolbar_main);
    if (showNavigationButtonInToolbar) {
      toolbar.setNavigationIcon(R.drawable.pspdf__ic_navigation_arrow);
      toolbar.setNavigationOnClickListener(v -> {
        if (reactPdfUiFragmentListener != null) {
          reactPdfUiFragmentListener.onNavigationButtonClicked(this);
        }
      });
    } else {
      toolbar.setNavigationIcon(null);
      toolbar.setNavigationOnClickListener(null);
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    // We want to get notified when a child PdfFragment is created so we can reattach our listeners.
    getChildFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
  }

  @Override
  public void onStop() {
    super.onStop();
    getChildFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
  }

  public void setEventDispatcher(EventDispatcher eventDispatcher, int viewId) {
    this.eventDispatcher = eventDispatcher;
    this.viewId = viewId;
  }

  /**
   * Listener that notifies of actions taken directly in the PdfUiFragment.
   */
  public interface ReactPdfUiFragmentListener {

    /**
     * Called when the configuration changed, reset your {@link com.pspdfkit.ui.PdfFragment} and {@link PdfUiFragment} listeners in here.
     */
    void onConfigurationChanged(@NonNull PdfUiFragment pdfUiFragment);

    /**
     * Called when the back navigation button was clicked.
     */
    void onNavigationButtonClicked(@NonNull PdfUiFragment pdfUiFragment);
  }

  @NonNull
  @Override
  public List<Integer> onGenerateMenuItemIds(@NonNull List<Integer> menuItems) {
    menuItems.clear();
    menuItems.add(R.id.menu_action_download);
    menuItems.add(PdfActivity.MENU_OPTION_SEARCH);
    menuItems.add(R.id.menu_action_rotate);
    return menuItems;
  }

  private void addMenuItem(@NonNull Menu menu,
                           @IdRes int menuId,
                           @StringRes int titleId,
                           @DrawableRes int iconId,
                           int order,
                           MenuItem.OnMenuItemClickListener menuItemListener) {

    MenuItem item = menu.add(Menu.NONE, menuId, order, titleId);
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    item.setIcon(iconId);
    item.setOnMenuItemClickListener(menuItemListener);

    // Apply toolbar theme color to custom menu item icon
    Drawable customIcon = Objects.requireNonNull(item.getIcon());
    final TypedArray a = requireContext().getTheme().obtainStyledAttributes(
      null,
      com.pspdfkit.R.styleable.pspdf__ActionBarIcons,
      com.pspdfkit.R.attr.pspdf__actionBarIconsStyle,
      com.pspdfkit.R.style.PSPDFKit_ActionBarIcons
    );
    int mainToolbarIconsColor = a.getColor(com.pspdfkit.R.styleable.pspdf__ActionBarIcons_pspdf__iconsColor,
      ContextCompat.getColor(requireContext(), android.R.color.white));
    a.recycle();
    DrawableCompat.setTint(customIcon, mainToolbarIconsColor);
    item.setIcon(customIcon);
  }

  private void downloadPdf() {
    if (eventDispatcher != null) {
      eventDispatcher.dispatchEvent(new DownloadPdfEvent(viewId));
    }
  }

  private void rotateDocument() {
    PdfDocument document = Objects.requireNonNull(getDocument());

    int pagesCount = document.getPageCount();

    for (int pageIndex = 0; pageIndex < pagesCount; pageIndex++) {
      // Get the existing rotation offset of the current page.
      int currentRotationOffset = document.getRotationOffset(pageIndex);

      // Add the desired rotation to the current offset.
      int newRotation = currentRotationOffset + 90;

      // Make sure that the new rotation offset is in bounds.
      if (newRotation < 0) {
        newRotation += 360;
      } else if (newRotation >= 360) {
        newRotation -= 360;
      }

      switch (newRotation) {
        case 0:
          newRotation = PdfDocument.NO_ROTATION;
          break;
        case 90:
          newRotation = PdfDocument.ROTATION_90;
          break;
        case 180:
          newRotation = PdfDocument.ROTATION_180;
          break;
        case 270:
          newRotation = PdfDocument.ROTATION_270;
          break;
        default:
          return;
      }

      // Applies a temporary rotation to the specified page of the document.
      // This will change the size reported by the document to match the new rotation.
      // The document will not be modified by this call.
      document.setRotationOffset(newRotation, pageIndex);
    }
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    MenuItem searchMenuItem = menu.findItem(PdfActivity.MENU_OPTION_SEARCH);
    menu.clear();

    addMenuItem(menu, R.id.menu_action_download, R.string.download, R.drawable.ic_download,
      0, item -> {
        downloadPdf();
        return true;
      });
    menu.add(Menu.NONE, PdfActivity.MENU_OPTION_SEARCH, 1, searchMenuItem.getTitle());

    addMenuItem(menu, R.id.menu_action_rotate, R.string.rotate, R.drawable.ic_rotate,
      2, item -> {
        rotateDocument();
        return true;
      });
  }
}
