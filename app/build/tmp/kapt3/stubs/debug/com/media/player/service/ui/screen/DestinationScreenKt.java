package com.media.player.service.ui.screen;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000D\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0005\u001az\u0010\u0000\u001a\u00020\u000120\u0010\u0002\u001a,\u0012\"\u0012 \u0012\u0004\u0012\u00020\u0005\u0012\u0016\u0012\u0014\u0012\u0004\u0012\u00020\u0005\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00060\u00040\u0004\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\b2\b\b\u0002\u0010\t\u001a\u00020\n2&\b\u0002\u0010\u000b\u001a \u0012\u0004\u0012\u00020\u0005\u0012\u0016\u0012\u0014\u0012\u0004\u0012\u00020\u0005\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00060\u00040\u0004H\u0007\u001a\b\u0010\f\u001a\u00020\u0001H\u0007\u001aH\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000f2\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\u00032\u0018\u0010\u0011\u001a\u0014\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00010\u00122\b\b\u0002\u0010\t\u001a\u00020\nH\u0007\u001a\u0014\u0010\u0014\u001a\u00020\u00132\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0006\u001a2\u0010\u0016\u001a \u0012\u0004\u0012\u00020\u0005\u0012\u0016\u0012\u0014\u0012\u0004\u0012\u00020\u0005\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00060\u00040\u00042\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0006\u001a\"\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00062\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00062\u0006\u0010\u0018\u001a\u00020\u0005\u001a\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0006\u001a\"\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00062\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00062\u0006\u0010\u001b\u001a\u00020\u001c\u001a\"\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00062\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00062\u0006\u0010\u001e\u001a\u00020\u0005\u001a*\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00062\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00062\u0006\u0010\u001e\u001a\u00020\u00052\u0006\u0010 \u001a\u00020\u0013\u00a8\u0006!"}, d2 = {"DestinationScreen", "", "onSave", "Lkotlin/Function1;", "", "", "", "onBack", "Lkotlin/Function0;", "modifier", "Landroidx/compose/ui/Modifier;", "initialSelectedRegions", "DestinationScreenPreview", "RegionItem", "region", "Lcom/media/player/service/ui/screen/Region;", "onToggleExpand", "onToggleSelect", "Lkotlin/Function2;", "", "countSelectedRegions", "regions", "extractSelectedRegions", "filterRegions", "query", "getSampleRegions", "selectAllRegions", "selected", "", "toggleRegionExpansion", "regionName", "toggleRegionSelection", "level", "app_debug"})
public final class DestinationScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void DestinationScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.util.Map<java.lang.String, ? extends java.util.Map<java.lang.String, ? extends java.util.List<java.lang.String>>>, kotlin.Unit> onSave, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull
    java.util.Map<java.lang.String, ? extends java.util.Map<java.lang.String, ? extends java.util.List<java.lang.String>>> initialSelectedRegions) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void RegionItem(@org.jetbrains.annotations.NotNull
    com.media.player.service.ui.screen.Region region, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onToggleExpand, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.Integer, kotlin.Unit> onToggleSelect, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.media.player.service.ui.screen.Region> getSampleRegions() {
        return null;
    }
    
    public static final int countSelectedRegions(@org.jetbrains.annotations.NotNull
    java.util.List<com.media.player.service.ui.screen.Region> regions) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.media.player.service.ui.screen.Region> selectAllRegions(@org.jetbrains.annotations.NotNull
    java.util.List<com.media.player.service.ui.screen.Region> regions, boolean selected) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.media.player.service.ui.screen.Region> toggleRegionExpansion(@org.jetbrains.annotations.NotNull
    java.util.List<com.media.player.service.ui.screen.Region> regions, @org.jetbrains.annotations.NotNull
    java.lang.String regionName) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.media.player.service.ui.screen.Region> toggleRegionSelection(@org.jetbrains.annotations.NotNull
    java.util.List<com.media.player.service.ui.screen.Region> regions, @org.jetbrains.annotations.NotNull
    java.lang.String regionName, int level) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.media.player.service.ui.screen.Region> filterRegions(@org.jetbrains.annotations.NotNull
    java.util.List<com.media.player.service.ui.screen.Region> regions, @org.jetbrains.annotations.NotNull
    java.lang.String query) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.util.List<java.lang.String>>> extractSelectedRegions(@org.jetbrains.annotations.NotNull
    java.util.List<com.media.player.service.ui.screen.Region> regions) {
        return null;
    }
    
    @androidx.compose.ui.tooling.preview.Preview(showBackground = true)
    @androidx.compose.runtime.Composable
    public static final void DestinationScreenPreview() {
    }
}