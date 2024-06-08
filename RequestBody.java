package dc.squareup.okhttp3;

import dc.squareup.okhttp3.internal.Util;
import dc.squareup.okio.BufferedSink;
import dc.squareup.okio.ByteString;
import dc.squareup.okio.Okio;
import dc.squareup.okio.Source;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;

public abstract class RequestBody {
    public static RequestBody create(MediaType mediaType, String str) {
        Charset charset;
        Charset charset2 = Util.UTF_8;
        if (!(mediaType == null || (charset = mediaType.charset()) == null)) {
            charset2 = charset;
        }
        return create(mediaType, str.getBytes(charset2));
    }

    public static RequestBody createWithBytes(MediaType mediaType, byte[] bArr) {
        return create(mediaType, bArr);
    }

    public long contentLength() throws IOException {
        return -1;
    }

    public abstract MediaType contentType();

    public abstract void writeTo(BufferedSink bufferedSink) throws IOException;

    public static RequestBody create(final MediaType mediaType, final ByteString byteString) {
        return new RequestBody() {
            public long contentLength() throws IOException {
                return (long) byteString.size();
            }

            public MediaType contentType() {
                return MediaType.this;
            }

            public void writeTo(BufferedSink bufferedSink) throws IOException {
                bufferedSink.write(byteString);
            }
        };
    }

    public static RequestBody create(MediaType mediaType, byte[] bArr) {
        return create(mediaType, bArr, 0, bArr.length);
    }

    public static RequestBody create(final MediaType mediaType, final byte[] bArr, final int i, final int i2) {
        Objects.requireNonNull(bArr, "content == null");
        Util.checkOffsetAndCount((long) bArr.length, (long) i, (long) i2);
        return new RequestBody() {
            public long contentLength() {
                return (long) i2;
            }

            public MediaType contentType() {
                return MediaType.this;
            }

            public void writeTo(BufferedSink bufferedSink) throws IOException {
                bufferedSink.write(bArr, i, i2);
            }
        };
    }

    public static RequestBody create(final MediaType mediaType, final File file) {
        Objects.requireNonNull(file, "file == null");
        return new RequestBody() {
            public long contentLength() {
                return file.length();
            }

            public MediaType contentType() {
                return MediaType.this;
            }

            public void writeTo(BufferedSink bufferedSink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(file);
                    bufferedSink.writeAll(source);
                } finally {
                    Util.closeQuietly((Closeable) source);
                }
            }
        };
    }
}
