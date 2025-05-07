package org.oagi.score.gateway.http.common.util;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;

public class DeleteOnCloseFileSystemResource extends FileSystemResource {

    public DeleteOnCloseFileSystemResource(String path) {
        super(path);
    }

    public DeleteOnCloseFileSystemResource(File file) {
        super(file);
    }

    public DeleteOnCloseFileSystemResource(Path filePath) {
        super(filePath);
    }

    public DeleteOnCloseFileSystemResource(FileSystem fileSystem, String path) {
        super(fileSystem, path);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new DeleteOnCloseFileInputStream(super.getInputStream(), getFile());
    }

    private class DeleteOnCloseFileInputStream extends InputStream {

        private InputStream delegate;

        private File file;

        DeleteOnCloseFileInputStream(InputStream delegate, File file) {
            this.delegate = delegate;
            this.file = file;
        }

        public static InputStream nullInputStream() {
            return InputStream.nullInputStream();
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return delegate.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public byte[] readAllBytes() throws IOException {
            return delegate.readAllBytes();
        }

        @Override
        public byte[] readNBytes(int len) throws IOException {
            return delegate.readNBytes(len);
        }

        @Override
        public int readNBytes(byte[] b, int off, int len) throws IOException {
            return delegate.readNBytes(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        @Override
        public void skipNBytes(long n) throws IOException {
            delegate.skipNBytes(n);
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
            FileUtils.deleteQuietly(file);
        }

        @Override
        public void mark(int readlimit) {
            delegate.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }

        @Override
        public long transferTo(OutputStream out) throws IOException {
            return delegate.transferTo(out);
        }
    }
}
