package com.media.player.service.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\"\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\b\u0013\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\bJ\u000e\u0010$\u001a\u00020\"2\u0006\u0010#\u001a\u00020\bJ\b\u0010%\u001a\u00020\"H\u0002J\b\u0010&\u001a\u00020\"H\u0002J\u000e\u0010\'\u001a\u00020\"2\u0006\u0010#\u001a\u00020\bJ\u000e\u0010(\u001a\u00020\"2\u0006\u0010#\u001a\u00020\bJ\b\u0010)\u001a\u00020\"H\u0002J\b\u0010*\u001a\u00020\"H\u0002J\u0006\u0010+\u001a\u00020\"J\u000e\u0010,\u001a\u00020\"2\u0006\u0010-\u001a\u00020\nJ\u0014\u0010.\u001a\u00020\"2\f\u0010/\u001a\b\u0012\u0004\u0012\u00020\b0\u0011J\u000e\u00100\u001a\u00020\"2\u0006\u00101\u001a\u00020\fJ\b\u00102\u001a\u00020\"H\u0002J\u000e\u00103\u001a\u00020\"2\u0006\u00104\u001a\u00020\u000eR\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00110\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0017\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\n0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0015R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\f0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0015R\u0017\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0015R\u001d\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0015R\u001d\u0010\u001f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00110\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0015\u00a8\u00065"}, d2 = {"Lcom/media/player/service/viewmodel/MainViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "_acceptKeywords", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "", "_callMode", "Lcom/media/player/service/viewmodel/CallMode;", "_distanceRange", "", "_isServiceRunning", "", "_rejectKeywords", "_selectedDestinations", "", "acceptKeywords", "Lkotlinx/coroutines/flow/StateFlow;", "getAcceptKeywords", "()Lkotlinx/coroutines/flow/StateFlow;", "callMode", "getCallMode", "context", "Landroid/content/Context;", "distanceRange", "getDistanceRange", "isServiceRunning", "rejectKeywords", "getRejectKeywords", "selectedDestinations", "getSelectedDestinations", "addAcceptKeyword", "", "keyword", "addRejectKeyword", "checkAccessibilityService", "loadSettings", "removeAcceptKeyword", "removeRejectKeyword", "startService", "stopService", "toggleService", "updateCallMode", "mode", "updateDestinations", "destinations", "updateDistance", "distance", "updateKeywordsInDataStore", "updateServiceStatus", "isRunning", "app_debug"})
public final class MainViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isServiceRunning = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isServiceRunning = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.media.player.service.viewmodel.CallMode> _callMode = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.media.player.service.viewmodel.CallMode> callMode = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.Set<java.lang.String>> _selectedDestinations = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.Set<java.lang.String>> selectedDestinations = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Float> _distanceRange = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Float> distanceRange = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<java.lang.String>> _acceptKeywords = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.String>> acceptKeywords = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<java.lang.String>> _rejectKeywords = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.String>> rejectKeywords = null;
    
    public MainViewModel(@org.jetbrains.annotations.NotNull
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isServiceRunning() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.media.player.service.viewmodel.CallMode> getCallMode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.Set<java.lang.String>> getSelectedDestinations() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Float> getDistanceRange() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.String>> getAcceptKeywords() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.String>> getRejectKeywords() {
        return null;
    }
    
    private final void loadSettings() {
    }
    
    public final void updateCallMode(@org.jetbrains.annotations.NotNull
    com.media.player.service.viewmodel.CallMode mode) {
    }
    
    public final void updateDestinations(@org.jetbrains.annotations.NotNull
    java.util.Set<java.lang.String> destinations) {
    }
    
    public final void updateDistance(float distance) {
    }
    
    public final void addAcceptKeyword(@org.jetbrains.annotations.NotNull
    java.lang.String keyword) {
    }
    
    public final void removeAcceptKeyword(@org.jetbrains.annotations.NotNull
    java.lang.String keyword) {
    }
    
    public final void addRejectKeyword(@org.jetbrains.annotations.NotNull
    java.lang.String keyword) {
    }
    
    public final void removeRejectKeyword(@org.jetbrains.annotations.NotNull
    java.lang.String keyword) {
    }
    
    private final void updateKeywordsInDataStore() {
    }
    
    public final void toggleService() {
    }
    
    public final void updateServiceStatus(boolean isRunning) {
    }
    
    private final void startService() {
    }
    
    private final void stopService() {
    }
    
    private final void checkAccessibilityService() {
    }
}