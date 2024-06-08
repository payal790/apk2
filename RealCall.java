package dc.squareup.okhttp3;

import androidx.core.app.NotificationCompat;
import dc.squareup.okhttp3.internal.NamedRunnable;
import dc.squareup.okhttp3.internal.Util;
import dc.squareup.okhttp3.internal.cache.CacheInterceptor;
import dc.squareup.okhttp3.internal.connection.ConnectInterceptor;
import dc.squareup.okhttp3.internal.connection.RealConnection;
import dc.squareup.okhttp3.internal.connection.StreamAllocation;
import dc.squareup.okhttp3.internal.http.BridgeInterceptor;
import dc.squareup.okhttp3.internal.http.CallServerInterceptor;
import dc.squareup.okhttp3.internal.http.HttpCodec;
import dc.squareup.okhttp3.internal.http.RealInterceptorChain;
import dc.squareup.okhttp3.internal.http.RetryAndFollowUpInterceptor;
import dc.squareup.okhttp3.internal.platform.Platform;
import dc.squareup.okio.AsyncTimeout;
import dc.squareup.okio.Timeout;
import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

final class RealCall implements Call {
    private static final Comparator<String> FIELD_NAME_COMPARATOR = new Comparator<String>() {
        public int compare(String str, String str2) {
            if (str == str2) {
                return 0;
            }
            if (str == null) {
                return -1;
            }
            if (str2 == null) {
                return 1;
            }
            return String.CASE_INSENSITIVE_ORDER.compare(str, str2);
        }
    };
    final OkHttpClient client;
    /* access modifiers changed from: private */
    public EventListener eventListener;
    private boolean executed;
    final boolean forWebSocket;
    final Request originalRequest;
    final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;
    final AsyncTimeout timeout;

    private RealCall(OkHttpClient okHttpClient, Request request, boolean z) {
        this.client = okHttpClient;
        this.originalRequest = request;
        this.forWebSocket = z;
        this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(okHttpClient, z);
        AnonymousClass1 r4 = new AsyncTimeout() {
            /* access modifiers changed from: protected */
            public void timedOut() {
                RealCall.this.cancel();
            }
        };
        this.timeout = r4;
        r4.timeout((long) okHttpClient.callTimeoutMillis(), TimeUnit.MILLISECONDS);
    }

    private void captureCallStackTrace() {
        this.retryAndFollowUpInterceptor.setCallStackTrace(Platform.get().getStackTraceForCloseable("response.body().close()"));
    }

    static RealCall newRealCall(OkHttpClient okHttpClient, Request request, boolean z) {
        RealCall realCall = new RealCall(okHttpClient, request, z);
        realCall.eventListener = okHttpClient.eventListenerFactory().create(realCall);
        return realCall;
    }

    public static Map<String, List<String>> toMultimap(Headers headers, String str) {
        TreeMap treeMap = new TreeMap(FIELD_NAME_COMPARATOR);
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            String name = headers.name(i);
            String value = headers.value(i);
            ArrayList arrayList = new ArrayList();
            List list = (List) treeMap.get(name);
            if (list != null) {
                arrayList.addAll(list);
            }
            arrayList.add(value);
            treeMap.put(name, Collections.unmodifiableList(arrayList));
        }
        if (str != null) {
            treeMap.put((Object) null, Collections.unmodifiableList(Collections.singletonList(str)));
        }
        return Collections.unmodifiableMap(treeMap);
    }

    public void cancel() {
        this.retryAndFollowUpInterceptor.cancel();
    }

    public void enqueue(Callback callback) {
        synchronized (this) {
            if (!this.executed) {
                this.executed = true;
            } else {
                throw new IllegalStateException("Already Executed");
            }
        }
        captureCallStackTrace();
        this.eventListener.callStart(this);
        this.client.dispatcher().enqueue(new AsyncCall(callback));
    }

    public Response execute() throws IOException {
        synchronized (this) {
            if (!this.executed) {
                this.executed = true;
            } else {
                throw new IllegalStateException("Already Executed");
            }
        }
        captureCallStackTrace();
        this.timeout.enter();
        this.eventListener.callStart(this);
        try {
            this.client.dispatcher().executed(this);
            Response responseWithInterceptorChain = getResponseWithInterceptorChain();
            if (responseWithInterceptorChain != null) {
                this.client.dispatcher().finished(this);
                return responseWithInterceptorChain;
            }
            throw new IOException("Canceled");
        } catch (IOException e) {
            IOException timeoutExit = timeoutExit(e);
            this.eventListener.callFailed(this, timeoutExit);
            throw timeoutExit;
        } catch (Throwable th) {
            this.client.dispatcher().finished(this);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public Response getResponseWithInterceptorChain() throws IOException {
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(this.client.interceptors());
        arrayList.add(this.retryAndFollowUpInterceptor);
        arrayList.add(new BridgeInterceptor(this.client.cookieJar()));
        arrayList.add(new CacheInterceptor(this.client.internalCache()));
        arrayList.add(new ConnectInterceptor(this.client));
        if (!this.forWebSocket) {
            arrayList.addAll(this.client.networkInterceptors());
        }
        arrayList.add(new CallServerInterceptor(this.forWebSocket));
        Response proceed = new RealInterceptorChain(arrayList, (StreamAllocation) null, (HttpCodec) null, (RealConnection) null, 0, this.originalRequest, this, this.eventListener, this.client.connectTimeoutMillis(), this.client.readTimeoutMillis(), this.client.writeTimeoutMillis()).proceed(this.originalRequest);
        if (!this.retryAndFollowUpInterceptor.isCanceled()) {
            return proceed;
        }
        Util.closeQuietly((Closeable) proceed);
        throw new IOException("Canceled");
    }

    public boolean isCanceled() {
        return this.retryAndFollowUpInterceptor.isCanceled();
    }

    public synchronized boolean isExecuted() {
        return this.executed;
    }

    /* access modifiers changed from: package-private */
    public String redactedUrl() {
        return this.originalRequest.url().redact();
    }

    public Request request() {
        return this.originalRequest;
    }

    /* access modifiers changed from: package-private */
    public StreamAllocation streamAllocation() {
        return this.retryAndFollowUpInterceptor.streamAllocation();
    }

    public Timeout timeout() {
        return this.timeout;
    }

    /* access modifiers changed from: package-private */
    public IOException timeoutExit(IOException iOException) {
        if (!this.timeout.exit()) {
            return iOException;
        }
        InterruptedIOException interruptedIOException = new InterruptedIOException("timeout");
        if (iOException != null) {
            interruptedIOException.initCause(iOException);
        }
        return interruptedIOException;
    }

    /* access modifiers changed from: package-private */
    public String toLoggableString() {
        StringBuilder sb = new StringBuilder();
        sb.append(isCanceled() ? "canceled " : "");
        sb.append(this.forWebSocket ? "web socket" : NotificationCompat.CATEGORY_CALL);
        sb.append(" to ");
        sb.append(redactedUrl());
        return sb.toString();
    }

    final class AsyncCall extends NamedRunnable {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final Callback responseCallback;

        static {
            Class<RealCall> cls = RealCall.class;
        }

        AsyncCall(Callback callback) {
            super("OkHttp %s", RealCall.this.redactedUrl());
            this.responseCallback = callback;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0033 A[Catch:{ IOException -> 0x0051, all -> 0x002b, all -> 0x0098 }] */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x005a A[Catch:{ IOException -> 0x0051, all -> 0x002b, all -> 0x0098 }] */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x007a A[Catch:{ IOException -> 0x0051, all -> 0x002b, all -> 0x0098 }] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void execute() {
            /*
                r5 = this;
                dc.squareup.okhttp3.RealCall r0 = dc.squareup.okhttp3.RealCall.this
                dc.squareup.okio.AsyncTimeout r0 = r0.timeout
                r0.enter()
                r0 = 0
                dc.squareup.okhttp3.RealCall r1 = dc.squareup.okhttp3.RealCall.this     // Catch:{ IOException -> 0x0051, all -> 0x002b }
                dc.squareup.okhttp3.Response r0 = r1.getResponseWithInterceptorChain()     // Catch:{ IOException -> 0x0051, all -> 0x002b }
                r1 = 1
                dc.squareup.okhttp3.Callback r2 = r5.responseCallback     // Catch:{ IOException -> 0x0027, all -> 0x0023 }
                dc.squareup.okhttp3.RealCall r3 = dc.squareup.okhttp3.RealCall.this     // Catch:{ IOException -> 0x0027, all -> 0x0023 }
                r2.onResponse(r3, r0)     // Catch:{ IOException -> 0x0027, all -> 0x0023 }
                dc.squareup.okhttp3.RealCall r0 = dc.squareup.okhttp3.RealCall.this
                dc.squareup.okhttp3.OkHttpClient r0 = r0.client
                dc.squareup.okhttp3.Dispatcher r0 = r0.dispatcher()
                r0.finished((dc.squareup.okhttp3.RealCall.AsyncCall) r5)
                goto L_0x0097
            L_0x0023:
                r0 = move-exception
                r1 = r0
                r0 = 1
                goto L_0x002c
            L_0x0027:
                r0 = move-exception
                r1 = r0
                r0 = 1
                goto L_0x0052
            L_0x002b:
                r1 = move-exception
            L_0x002c:
                dc.squareup.okhttp3.RealCall r2 = dc.squareup.okhttp3.RealCall.this     // Catch:{ all -> 0x0098 }
                r2.cancel()     // Catch:{ all -> 0x0098 }
                if (r0 != 0) goto L_0x0050
                java.io.IOException r0 = new java.io.IOException     // Catch:{ all -> 0x0098 }
                java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0098 }
                r2.<init>()     // Catch:{ all -> 0x0098 }
                java.lang.String r3 = "canceled due to "
                r2.append(r3)     // Catch:{ all -> 0x0098 }
                r2.append(r1)     // Catch:{ all -> 0x0098 }
                java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0098 }
                r0.<init>(r2)     // Catch:{ all -> 0x0098 }
                dc.squareup.okhttp3.Callback r2 = r5.responseCallback     // Catch:{ all -> 0x0098 }
                dc.squareup.okhttp3.RealCall r3 = dc.squareup.okhttp3.RealCall.this     // Catch:{ all -> 0x0098 }
                r2.onFailure(r3, r0)     // Catch:{ all -> 0x0098 }
            L_0x0050:
                throw r1     // Catch:{ all -> 0x0098 }
            L_0x0051:
                r1 = move-exception
            L_0x0052:
                dc.squareup.okhttp3.RealCall r2 = dc.squareup.okhttp3.RealCall.this     // Catch:{ all -> 0x0098 }
                java.io.IOException r1 = r2.timeoutExit(r1)     // Catch:{ all -> 0x0098 }
                if (r0 == 0) goto L_0x007a
                dc.squareup.okhttp3.internal.platform.Platform r0 = dc.squareup.okhttp3.internal.platform.Platform.get()     // Catch:{ all -> 0x0098 }
                r2 = 4
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0098 }
                r3.<init>()     // Catch:{ all -> 0x0098 }
                java.lang.String r4 = "Callback failure for "
                r3.append(r4)     // Catch:{ all -> 0x0098 }
                dc.squareup.okhttp3.RealCall r4 = dc.squareup.okhttp3.RealCall.this     // Catch:{ all -> 0x0098 }
                java.lang.String r4 = r4.toLoggableString()     // Catch:{ all -> 0x0098 }
                r3.append(r4)     // Catch:{ all -> 0x0098 }
                java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0098 }
                r0.log(r2, r3, r1)     // Catch:{ all -> 0x0098 }
                goto L_0x008c
            L_0x007a:
                dc.squareup.okhttp3.RealCall r0 = dc.squareup.okhttp3.RealCall.this     // Catch:{ all -> 0x0098 }
                dc.squareup.okhttp3.EventListener r0 = r0.eventListener     // Catch:{ all -> 0x0098 }
                dc.squareup.okhttp3.RealCall r2 = dc.squareup.okhttp3.RealCall.this     // Catch:{ all -> 0x0098 }
                r0.callFailed(r2, r1)     // Catch:{ all -> 0x0098 }
                dc.squareup.okhttp3.Callback r0 = r5.responseCallback     // Catch:{ all -> 0x0098 }
                dc.squareup.okhttp3.RealCall r2 = dc.squareup.okhttp3.RealCall.this     // Catch:{ all -> 0x0098 }
                r0.onFailure(r2, r1)     // Catch:{ all -> 0x0098 }
            L_0x008c:
                dc.squareup.okhttp3.RealCall r0 = dc.squareup.okhttp3.RealCall.this
                dc.squareup.okhttp3.OkHttpClient r0 = r0.client
                dc.squareup.okhttp3.Dispatcher r0 = r0.dispatcher()
                r0.finished((dc.squareup.okhttp3.RealCall.AsyncCall) r5)
            L_0x0097:
                return
            L_0x0098:
                r0 = move-exception
                dc.squareup.okhttp3.RealCall r1 = dc.squareup.okhttp3.RealCall.this
                dc.squareup.okhttp3.OkHttpClient r1 = r1.client
                dc.squareup.okhttp3.Dispatcher r1 = r1.dispatcher()
                r1.finished((dc.squareup.okhttp3.RealCall.AsyncCall) r5)
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: dc.squareup.okhttp3.RealCall.AsyncCall.execute():void");
        }

        /* access modifiers changed from: package-private */
        public RealCall get() {
            return RealCall.this;
        }

        /* access modifiers changed from: package-private */
        public String host() {
            return RealCall.this.originalRequest.url().host();
        }

        /* access modifiers changed from: package-private */
        public Request request() {
            return RealCall.this.originalRequest;
        }

        /* access modifiers changed from: package-private */
        public void executeOn(ExecutorService executorService) {
            try {
                executorService.execute(this);
            } catch (RejectedExecutionException e) {
                InterruptedIOException interruptedIOException = new InterruptedIOException("executor rejected");
                interruptedIOException.initCause(e);
                RealCall.this.eventListener.callFailed(RealCall.this, interruptedIOException);
                this.responseCallback.onFailure(RealCall.this, interruptedIOException);
                RealCall.this.client.dispatcher().finished(this);
            } catch (Throwable th) {
                RealCall.this.client.dispatcher().finished(this);
                throw th;
            }
        }
    }

    public RealCall clone() {
        return newRealCall(this.client, this.originalRequest, this.forWebSocket);
    }
}
