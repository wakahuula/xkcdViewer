package de.stoupas.xkcd;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/** Test-only provider implementing the document protocol, not ContentResolver.delete. */
public class ExportTestDocumentsProvider extends ContentProvider {
    private File file(String id) { return new File(getContext().getCacheDir(), "export-" + id); }
    @Override public boolean onCreate() { return true; }
    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] args, String order) {
        String id = DocumentsContract.getDocumentId(uri);
        MatrixCursor cursor = new MatrixCursor(new String[]{"document_id", "_size"});
        if (file(id).exists()) cursor.addRow(new Object[]{id, file(id).length()});
        return cursor;
    }
    @Override public Bundle call(String method, String arg, Bundle extras) {
        Bundle result = new Bundle();
        if ("android:createDocument".equals(method)) {
            String name = extras.getString("_display_name");
            try { if (!file(name).createNewFile()) throw new IllegalStateException("Duplicate document"); }
            catch (IOException failure) { throw new IllegalStateException(failure); }
            result.putParcelable("uri", DocumentsContract.buildDocumentUri("de.stoupas.xkcd.test.documents", name));
        } else if ("android:deleteDocument".equals(method)) {
            Uri uri = extras.getParcelable("uri");
            if (!file(DocumentsContract.getDocumentId(uri)).delete()) throw new IllegalStateException("Delete failed");
        } else { throw new UnsupportedOperationException(method); }
        return result;
    }
    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        String id = DocumentsContract.getDocumentId(uri);
        if (id.startsWith("fail")) throw new FileNotFoundException("Test write failure");
        return ParcelFileDescriptor.open(file(id), ParcelFileDescriptor.parseMode(mode));
    }
    @Override public String getType(Uri uri) { return "image/png"; }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException(); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] args) { return 0; }
    @Override public int delete(Uri uri, String selection, String[] args) {
        throw new UnsupportedOperationException("Use DocumentsContract.deleteDocument");
    }
}
