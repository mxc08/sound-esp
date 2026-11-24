package com.example.soundesp;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SoundEspScreen extends Screen {
    private static final int ROWS = 8;

    private final List<String> allIds = new ArrayList<>();
    private List<String> filtered = new ArrayList<>();
    private final List<Button> rowButtons = new ArrayList<>();
    private EditBox search;
    private Button pageLabel;
    private int page = 0;
    private String query = "";

    public SoundEspScreen() {
        super(Component.literal("Sound ESP"));
        BuiltInRegistries.SOUND_EVENT.keySet().forEach(id -> allIds.add(id.toString()));
        allIds.sort(String::compareTo);
    }

    @Override
    protected void init() {
        SoundEspConfig cfg = SoundEspConfig.get();
        int cx = width / 2;
        int left = cx - 150;
        int y = 28;

        addRenderableWidget(Button.builder(enabledText(), b -> {
            cfg.enabled = !cfg.enabled;
            b.setMessage(enabledText());
        }).bounds(left, y, 148, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Clear markers"),
                b -> MarkerManager.clear()).bounds(left + 152, y, 148, 20).build());
        y += 24;

        addRenderableWidget(new AbstractSliderButton(left, y, 148, 20, Component.empty(), (cfg.range - 4) / 252.0) {
            { updateMessage(); }
            @Override protected void updateMessage() { setMessage(Component.literal("Range: " + (4 + (int) Math.round(value * 252)) + " blocks")); }
            @Override protected void applyValue() { cfg.range = 4 + (int) Math.round(value * 252); }
        });
        addRenderableWidget(new AbstractSliderButton(left + 152, y, 148, 20, Component.empty(), (cfg.lifetimeSeconds - 1) / 29.0) {
            { updateMessage(); }
            @Override protected void updateMessage() { setMessage(Component.literal("Lifetime: " + (1 + (int) Math.round(value * 29)) + " s")); }
            @Override protected void applyValue() { cfg.lifetimeSeconds = 1 + (int) Math.round(value * 29); }
        });
        y += 28;

        search = new EditBox(font, left, y, 300, 18, Component.literal("Search"));
        search.setHint(Component.literal("Search sound IDs (e.g. zombie, chest, door)"));
        search.setMaxLength(100);
        search.setValue(query);
        search.setResponder(s -> { query = s.toLowerCase(Locale.ROOT).trim(); page = 0; refilter(); });
        addRenderableWidget(search);
        y += 24;

        rowButtons.clear();
        for (int i = 0; i < ROWS; i++) {
            final int idx = i;
            Button b = Button.builder(Component.empty(), btn -> toggleRow(idx))
                    .bounds(left, y + i * 21, 300, 20).build();
            rowButtons.add(b);
            addRenderableWidget(b);
        }
        y += ROWS * 21 + 4;

        addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; updateRows(); })
                .bounds(left, y, 40, 20).build());
        pageLabel = addRenderableWidget(Button.builder(Component.empty(), b -> {})
                .bounds(left + 44, y, 212, 20).build());
        pageLabel.active = false;
        addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; updateRows(); })
                .bounds(left + 260, y, 40, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(cx - 100, y, 200, 20).build());

        refilter();
        setInitialFocus(search);
    }

    private Component enabledText() {
        return Component.literal("ESP: " + (SoundEspConfig.get().enabled ? "ON" : "OFF"));
    }

    private void refilter() {
        filtered = new ArrayList<>();
        for (String id : allIds) if (query.isEmpty() || id.contains(query)) filtered.add(id);
        updateRows();
    }

    private int pageCount() { return Math.max(1, (filtered.size() + ROWS - 1) / ROWS); }

    private void updateRows() {
        page = Math.max(0, Math.min(page, pageCount() - 1));
        for (int i = 0; i < ROWS; i++) {
            Button b = rowButtons.get(i);
            int idx = page * ROWS + i;
            if (idx < filtered.size()) {
                String id = filtered.get(idx);
                b.visible = true;
                b.setMessage(Component.literal((SoundEspConfig.get().selected.contains(id) ? "[x] " : "[  ] ") + id));
            } else {
                b.visible = false;
            }
        }
        if (pageLabel != null) {
            pageLabel.setMessage(Component.literal("Page " + (page + 1) + "/" + pageCount() + "  (" + filtered.size()
                    + " sounds, " + SoundEspConfig.get().selected.size() + " selected)"));
        }
    }

    private void toggleRow(int i) {
        int idx = page * ROWS + i;
        if (idx >= filtered.size()) return;
        String id = filtered.get(idx);
        var sel = SoundEspConfig.get().selected;
        if (!sel.remove(id)) sel.add(id);
        updateRows();
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (v != 0) { page += v < 0 ? 1 : -1; updateRows(); return true; }
        return super.mouseScrolled(mx, my, h, v);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        String t = "Sound ESP";
        g.text(font, t, width / 2 - font.width(t) / 2, 12, 0xFFFFFFFF, true);
    }

    @Override
    public void removed() {
        SoundEspConfig.save();
    }
}
