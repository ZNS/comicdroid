package com.zns.comicdroid.util;

import java.io.IOException;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class DriveUtil {
	
	public static File getFile(Drive service, String parent, String title) throws UserRecoverableAuthException, IOException {
		return getFile(service, null, parent, title);
	}
	
	//This tries to solve a google drive bug which can occur if you search both on parent and title
	public static File getFile(Drive service, String fileId, String parent, String title) throws UserRecoverableAuthException, IOException {
		try {
			if (fileId != null) {
				File f = service.files().get(fileId).execute();
				if (f != null) {
					return f;
				}
			}
		}
		catch (IOException e) {
			//Ignore this and try to get by searching instead...
		}
		
		try
		{
			FileList files = service.files().list().setQ("'" + parent + "' in parents and title = '" + title + "'").execute();
			if (files.size() > 0) {
				return files.getItems().get(0);
			}
		}
		catch (IOException e) {
			ChildList children = service.children().list(parent).execute();
			for (ChildReference child : children.getItems()) {
				File file = service.files().get(child.getId()).execute();
				if (file.getTitle().equalsIgnoreCase(title)) {
					return file;
				}
			}
		}
		return null;
	}
	
	/*public static void trimDriveFileRevisions(Drive service, String fileId, int revisionCount) throws IOException {
		RevisionList revisions = service.revisions().list(fileId).execute();
		if (revisions.getItems().size() > revisionCount)
		{
			List<Revision> items = revisions.getItems();
			Collections.sort(items, new RevisionsByDateComparer());
			for (Revision rev : items.subList(revisionCount, items.size())) {
				service.revisions().delete(fileId, rev.getId()).execute();
			}
		}
	}*/	
}