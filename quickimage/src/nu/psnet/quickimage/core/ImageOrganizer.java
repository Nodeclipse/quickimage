/*
 * Software released under Common Public License (CPL) v1.0
 */
package nu.psnet.quickimage.core;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IStorage;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * @author Per Salomonsson
 * 
 */
public class ImageOrganizer {
	public static final int VIEW_THUMB = 0;
	public static final int VIEW_FULLSIZE = 1;

	public String path;
	public String filename;
	private int index = 0;

	private ArrayList<ImageHolder> holders = new ArrayList<ImageHolder>();
	// private ImageHolder[] holders;
	private Display display;
	private int activeView = VIEW_FULLSIZE;
	private QManager manager;

	public ImageOrganizer(QManager manager, Display display) {
		this.manager = manager;
		this.display = display;
	}

	/**
	 * Can be used instead of setPath
	 *
	 */
	public void setStorage(IStorage storage) {
		this.path = null;
		this.filename = null;
		holders.clear();
		ImageHolder holder = new ImageHolder(manager, display);
		holder.setStorage(storage);
		holders.add(holder);
	}

	public void setPath(String path, String filename) {
		this.path = path;
		this.filename = filename;
		initList();
	}

	private ImageHolder holder(int index) {
		return (ImageHolder) holders.get(index);
	}

	public void removeHolder(ImageHolder holder) {
		holders.remove(holder);
		while (index >= holders.size() && index > 0)
			index--;

		manager.getImageEditor().setPartName(getCurrent().getDisplayName());
	}

	public void removeHolder(int index) {
		holders.remove(index);

		while (index >= holders.size() && index > 0)
			index--;
	}

	public ImageHolder getNext() {
		if (holder(index).getFullsize() != null && !holder(index).getFullsize().isDisposed())
			holder(index).getFullsize().dispose();

		index = (index + 1) % holders.size();

		return holder(index);
	}

	public int getCount() {
		return holders.size();
	}

	public boolean isSingle() {
		return getHolders().size() == 1;
	}

	public ArrayList<ImageHolder> getHolders() {
		return holders;
	}

	public ImageHolder getPrevious() {
		if (holder(index).getFullsize() != null && !holder(index).getFullsize().isDisposed())
			holder(index).getFullsize().dispose();

		if (index > 0)
			index--;

		return holder(index);
	}

	public boolean hasNext() {
		return (holders.size() - 1 != index);
	}

	public boolean hasPrevious() {
		return (index != 0);
	}

	public ImageHolder getCurrent() {
		return holder(index);
	}

	public int getTotalWidth() {
		return 0;
	}

	private void initList() {
		File f = new File(path);
		File[] files = f.listFiles(new ImageFileFilter());

		if (files != null) {
			String[] sfiles = new String[files.length];

			for (int i = 0; i < files.length; i++) {
				sfiles[i] = files[i].getAbsolutePath();
			}

			Arrays.sort(sfiles, String.CASE_INSENSITIVE_ORDER);

			for (int i = 0; i < sfiles.length; i++) {
				if (sfiles[i].endsWith(filename)) {
					index = i;
				}

				ImageHolder h = new ImageHolder(manager, display);
				h.setFile(new File(sfiles[i]));
				holders.add(h);
			}
		}
	}

	public Point getThumbWidth() {
		if (holders == null)
			return new Point(1, 1);

		return holder(0).getThumbDimension();
	}

	public int getActiveView() {
		return activeView;
	}

	public void setActiveView(int activeView) {
		this.activeView = activeView;
	}

	public boolean selectHolder(int x, int y) {
		boolean tmp = false;
		boolean found = false;
		for (int i = 0; i < holders.size(); i++) {
			tmp = holder(i).mouseClickedOver(x, y);
			if (tmp && !found) {
				if (holder(index).hasFullsize() && !holder(index).getFullsize().isDisposed())
					holder(index).getFullsize().dispose();
				index = i;
				found = true;
				holder(i).setSelected(true);
			} else
				holder(i).setSelected(false);
		}

		return found;
	}

	public void setCurrentToSelected() {
		for (int i = 0; i < holders.size(); i++) {
			if (holder(i).isSelected()) {
				holder(index).getFullsize().dispose();
				holder(i).setSelected(false);
			}
		}
		getCurrent().setSelected(true);
	}

	public void setSelectedToCurrent() {
		for (int i = 0; i < holders.size(); i++) {
			if (holder(i).isSelected()) {
				holder(index).getFullsize().dispose();
				index = i;
				break;
			}
		}
	}

	public void dispose() {
		for (int i = 0; i < holders.size(); i++) {
			holder(i).dispose();
		}
	}
}

class ImageFileFilter implements FileFilter {
	String[] patterns = new String[] { ".gif", ".jpeg", ".jpg", ".png", ".ico", ".bmp" };

	public boolean accept(File f) {
		for (int i = 0; i < patterns.length; i++) {
			if (f.getName().toLowerCase().endsWith(patterns[i]))
				return true;
		}

		return false;
	}
}
