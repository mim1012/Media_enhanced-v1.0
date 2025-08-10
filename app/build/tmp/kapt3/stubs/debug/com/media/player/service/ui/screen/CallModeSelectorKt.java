package com.media.player.service.ui.screen;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000*\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a0\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\b\b\u0002\u0010\b\u001a\u00020\tH\u0007\u001aR\u0010\n\u001a\u00020\u00012\b\b\u0002\u0010\u000b\u001a\u00020\u00032\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\r2\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\r2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\b\b\u0002\u0010\b\u001a\u00020\tH\u0007\u001a\b\u0010\u0010\u001a\u00020\u0001H\u0007\u00a8\u0006\u0011"}, d2 = {"CallModeOption", "", "mode", "Lcom/media/player/service/ui/screen/CallMode;", "isSelected", "", "onSelected", "Lkotlin/Function0;", "modifier", "Landroidx/compose/ui/Modifier;", "CallModeSelector", "selectedMode", "onModeSelected", "Lkotlin/Function1;", "onSave", "onBack", "CallModeSelectorPreview", "app_debug"})
public final class CallModeSelectorKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void CallModeSelector(@org.jetbrains.annotations.NotNull
    com.media.player.service.ui.screen.CallMode selectedMode, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.media.player.service.ui.screen.CallMode, kotlin.Unit> onModeSelected, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.media.player.service.ui.screen.CallMode, kotlin.Unit> onSave, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void CallModeOption(@org.jetbrains.annotations.NotNull
    com.media.player.service.ui.screen.CallMode mode, boolean isSelected, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onSelected, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.ui.tooling.preview.Preview(showBackground = true)
    @androidx.compose.runtime.Composable
    public static final void CallModeSelectorPreview() {
    }
}