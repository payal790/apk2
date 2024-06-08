package dc.squareup.okio;

import java.io.IOException;

public final class Pipe {
    final Buffer buffer = new Buffer();
    /* access modifiers changed from: private */
    public Sink foldedSink;
    final long maxBufferSize;
    private final Sink sink = new PipeSink();
    boolean sinkClosed;
    private final Source source = new PipeSource();
    boolean sourceClosed;

    final class PipeSink implements Sink {
        final PushableTimeout timeout = new PushableTimeout();

        PipeSink() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0041, code lost:
            if (r1 == null) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0043, code lost:
            r6.timeout.push(r1.timeout());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
            r1.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0055, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0056, code lost:
            r6.timeout.pop();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x005b, code lost:
            throw r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void close() throws java.io.IOException {
            /*
                r6 = this;
                dc.squareup.okio.Pipe r0 = dc.squareup.okio.Pipe.this
                dc.squareup.okio.Buffer r0 = r0.buffer
                monitor-enter(r0)
                dc.squareup.okio.Pipe r1 = dc.squareup.okio.Pipe.this     // Catch:{ all -> 0x005d }
                boolean r2 = r1.sinkClosed     // Catch:{ all -> 0x005d }
                if (r2 == 0) goto L_0x000d
                monitor-exit(r0)     // Catch:{ all -> 0x005d }
                return
            L_0x000d:
                dc.squareup.okio.Sink r1 = r1.foldedSink     // Catch:{ all -> 0x005d }
                if (r1 == 0) goto L_0x001a
                dc.squareup.okio.Pipe r1 = dc.squareup.okio.Pipe.this     // Catch:{ all -> 0x005d }
                dc.squareup.okio.Sink r1 = r1.foldedSink     // Catch:{ all -> 0x005d }
                goto L_0x0040
            L_0x001a:
                dc.squareup.okio.Pipe r1 = dc.squareup.okio.Pipe.this     // Catch:{ all -> 0x005d }
                boolean r2 = r1.sourceClosed     // Catch:{ all -> 0x005d }
                if (r2 == 0) goto L_0x0035
                dc.squareup.okio.Buffer r1 = r1.buffer     // Catch:{ all -> 0x005d }
                long r1 = r1.size()     // Catch:{ all -> 0x005d }
                r3 = 0
                int r5 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
                if (r5 > 0) goto L_0x002d
                goto L_0x0035
            L_0x002d:
                java.io.IOException r1 = new java.io.IOException     // Catch:{ all -> 0x005d }
                java.lang.String r2 = "source is closed"
                r1.<init>(r2)     // Catch:{ all -> 0x005d }
                throw r1     // Catch:{ all -> 0x005d }
            L_0x0035:
                dc.squareup.okio.Pipe r1 = dc.squareup.okio.Pipe.this     // Catch:{ all -> 0x005d }
                r2 = 1
                r1.sinkClosed = r2     // Catch:{ all -> 0x005d }
                dc.squareup.okio.Buffer r1 = r1.buffer     // Catch:{ all -> 0x005d }
                r1.notifyAll()     // Catch:{ all -> 0x005d }
                r1 = 0
            L_0x0040:
                monitor-exit(r0)     // Catch:{ all -> 0x005d }
                if (r1 == 0) goto L_0x005c
                dc.squareup.okio.PushableTimeout r0 = r6.timeout
                dc.squareup.okio.Timeout r2 = r1.timeout()
                r0.push(r2)
                r1.close()     // Catch:{ all -> 0x0055 }
                dc.squareup.okio.PushableTimeout r0 = r6.timeout
                r0.pop()
                goto L_0x005c
            L_0x0055:
                r0 = move-exception
                dc.squareup.okio.PushableTimeout r1 = r6.timeout
                r1.pop()
                throw r0
            L_0x005c:
                return
            L_0x005d:
                r1 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x005d }
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: dc.squareup.okio.Pipe.PipeSink.close():void");
        }

        public void flush() throws IOException {
            Sink sink;
            synchronized (Pipe.this.buffer) {
                Pipe pipe = Pipe.this;
                if (pipe.sinkClosed) {
                    throw new IllegalStateException("closed");
                } else if (pipe.foldedSink != null) {
                    sink = Pipe.this.foldedSink;
                } else {
                    Pipe pipe2 = Pipe.this;
                    if (pipe2.sourceClosed) {
                        if (pipe2.buffer.size() > 0) {
                            throw new IOException("source is closed");
                        }
                    }
                    sink = null;
                }
            }
            if (sink != null) {
                this.timeout.push(sink.timeout());
                try {
                    sink.flush();
                } finally {
                    this.timeout.pop();
                }
            }
        }

        public Timeout timeout() {
            return this.timeout;
        }

        public void write(Buffer buffer, long j) throws IOException {
            Sink sink;
            synchronized (Pipe.this.buffer) {
                if (!Pipe.this.sinkClosed) {
                    while (true) {
                        if (j <= 0) {
                            sink = null;
                            break;
                        } else if (Pipe.this.foldedSink != null) {
                            sink = Pipe.this.foldedSink;
                            break;
                        } else {
                            Pipe pipe = Pipe.this;
                            if (!pipe.sourceClosed) {
                                long size = pipe.maxBufferSize - pipe.buffer.size();
                                if (size == 0) {
                                    this.timeout.waitUntilNotified(Pipe.this.buffer);
                                } else {
                                    long min = Math.min(size, j);
                                    Pipe.this.buffer.write(buffer, min);
                                    j -= min;
                                    Pipe.this.buffer.notifyAll();
                                }
                            } else {
                                throw new IOException("source is closed");
                            }
                        }
                    }
                } else {
                    throw new IllegalStateException("closed");
                }
            }
            if (sink != null) {
                this.timeout.push(sink.timeout());
                try {
                    sink.write(buffer, j);
                } finally {
                    this.timeout.pop();
                }
            }
        }
    }

    final class PipeSource implements Source {
        final Timeout timeout = new Timeout();

        PipeSource() {
        }

        public void close() throws IOException {
            synchronized (Pipe.this.buffer) {
                Pipe pipe = Pipe.this;
                pipe.sourceClosed = true;
                pipe.buffer.notifyAll();
            }
        }

        public long read(Buffer buffer, long j) throws IOException {
            synchronized (Pipe.this.buffer) {
                if (!Pipe.this.sourceClosed) {
                    while (Pipe.this.buffer.size() == 0) {
                        Pipe pipe = Pipe.this;
                        if (pipe.sinkClosed) {
                            return -1;
                        }
                        this.timeout.waitUntilNotified(pipe.buffer);
                    }
                    long read = Pipe.this.buffer.read(buffer, j);
                    Pipe.this.buffer.notifyAll();
                    return read;
                }
                throw new IllegalStateException("closed");
            }
        }

        public Timeout timeout() {
            return this.timeout;
        }
    }

    public Pipe(long j) {
        if (j >= 1) {
            this.maxBufferSize = j;
            return;
        }
        throw new IllegalArgumentException("maxBufferSize < 1: " + j);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r8.write(r3, r3.size);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002f, code lost:
        if (r1 == false) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0031, code lost:
        r8.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0035, code lost:
        r8.flush();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0039, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003c, code lost:
        monitor-enter(r7.buffer);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r7.sourceClosed = true;
        r7.buffer.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0045, code lost:
        throw r8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void fold(dc.squareup.okio.Sink r8) throws java.io.IOException {
        /*
            r7 = this;
        L_0x0000:
            dc.squareup.okio.Buffer r0 = r7.buffer
            monitor-enter(r0)
            dc.squareup.okio.Sink r1 = r7.foldedSink     // Catch:{ all -> 0x0051 }
            if (r1 != 0) goto L_0x0049
            dc.squareup.okio.Buffer r1 = r7.buffer     // Catch:{ all -> 0x0051 }
            boolean r1 = r1.exhausted()     // Catch:{ all -> 0x0051 }
            r2 = 1
            if (r1 == 0) goto L_0x0016
            r7.sourceClosed = r2     // Catch:{ all -> 0x0051 }
            r7.foldedSink = r8     // Catch:{ all -> 0x0051 }
            monitor-exit(r0)     // Catch:{ all -> 0x0051 }
            return
        L_0x0016:
            boolean r1 = r7.sinkClosed     // Catch:{ all -> 0x0051 }
            dc.squareup.okio.Buffer r3 = new dc.squareup.okio.Buffer     // Catch:{ all -> 0x0051 }
            r3.<init>()     // Catch:{ all -> 0x0051 }
            dc.squareup.okio.Buffer r4 = r7.buffer     // Catch:{ all -> 0x0051 }
            long r5 = r4.size     // Catch:{ all -> 0x0051 }
            r3.write((dc.squareup.okio.Buffer) r4, (long) r5)     // Catch:{ all -> 0x0051 }
            dc.squareup.okio.Buffer r4 = r7.buffer     // Catch:{ all -> 0x0051 }
            r4.notifyAll()     // Catch:{ all -> 0x0051 }
            monitor-exit(r0)     // Catch:{ all -> 0x0051 }
            long r4 = r3.size     // Catch:{ all -> 0x0039 }
            r8.write(r3, r4)     // Catch:{ all -> 0x0039 }
            if (r1 == 0) goto L_0x0035
            r8.close()     // Catch:{ all -> 0x0039 }
            goto L_0x0000
        L_0x0035:
            r8.flush()     // Catch:{ all -> 0x0039 }
            goto L_0x0000
        L_0x0039:
            r8 = move-exception
            dc.squareup.okio.Buffer r1 = r7.buffer
            monitor-enter(r1)
            r7.sourceClosed = r2     // Catch:{ all -> 0x0046 }
            dc.squareup.okio.Buffer r0 = r7.buffer     // Catch:{ all -> 0x0046 }
            r0.notifyAll()     // Catch:{ all -> 0x0046 }
            monitor-exit(r1)     // Catch:{ all -> 0x0046 }
            throw r8
        L_0x0046:
            r8 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0046 }
            throw r8
        L_0x0049:
            java.lang.IllegalStateException r8 = new java.lang.IllegalStateException     // Catch:{ all -> 0x0051 }
            java.lang.String r1 = "sink already folded"
            r8.<init>(r1)     // Catch:{ all -> 0x0051 }
            throw r8     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r8 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0055
        L_0x0054:
            throw r8
        L_0x0055:
            goto L_0x0054
        */
        throw new UnsupportedOperationException("Method not decompiled: dc.squareup.okio.Pipe.fold(dc.squareup.okio.Sink):void");
    }

    public final Sink sink() {
        return this.sink;
    }

    public final Source source() {
        return this.source;
    }
}
