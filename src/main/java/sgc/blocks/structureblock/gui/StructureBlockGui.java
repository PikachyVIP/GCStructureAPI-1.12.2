package sgc.blocks.structureblock.gui;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import sgc.Main;
import sgc.api.gui.BlockPreviewRenderer;
import sgc.blocks.structureblock.StructureBlockTile;
import sgc.supporting.pakets.PacketToggleIgnoreAir;
import sgc.supporting.pakets.PacketUpdateCorners;
import sgc.supporting.pakets.PacketSaveStructure;
import sgc.supporting.pakets.PacketLoadStructure;
import sgc.supporting.pakets.PacketReadNbtFile;
import sgc.supporting.pakets.PacketUpdateLoadPosition;
import sgc.supporting.pakets.PacketUpdatePreview;
import sgc.supporting.pakets.PacketUpdateRotation;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.awt.Desktop;
import java.io.File;

public class StructureBlockGui extends GuiContainer {
    private final StructureBlockTile te;
    private int guiLeft, guiTop;
    private final int guiWidth = 256;
    private final int guiHeight = 200;

    private GuiTextField corner1X, corner1Y, corner1Z;
    private GuiTextField corner2X, corner2Y, corner2Z;
    private GuiTextField nameField;
    private GuiButton applyButton, saveButton;
    private GuiButton airToggleButton;
    private boolean ignoreAir = false;

    private static final int PREVIEW_SIZE = 150;
    private int previewX, previewY;
    private NBTTagCompound loadedNbt = null;
    private float zoomLevel = 1.0F;
    private GuiButton modeToggleButton;
    private GuiButton openFolderButton;
    private GuiButton loadButton;
    private GuiButton readNbtButton;
    private GuiButton visibilityToggleButton;
    private GuiButton rotateButton;
    private GuiTextField nbtFileNameField;
    private GuiTextField loadPosX, loadPosY, loadPosZ;
    private boolean isLoadMode = false;
    private boolean showPlacementPreview = true;
    private int rotation = 0;
    private BlockPos loadPosition = BlockPos.ORIGIN;
    private String loadedStructureName = "";

    public StructureBlockGui(IInventory pInv, StructureBlockTile tile) {
        super(new StructureBlockContainer(pInv, tile));
        this.te = tile;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.guiLeft = (width - guiWidth) / 2;
        this.guiTop = (height - guiHeight) / 2;

        int centerX = guiLeft + guiWidth / 2;
        int startY = guiTop + 30;
        int labelWidth = 40;
        int fieldWidth = 50;
        int spacing = 10;
        int totalWidth = labelWidth + fieldWidth;
        int startX = centerX - totalWidth - spacing;

        corner1X = new GuiTextField(0, fontRenderer, startX + labelWidth, startY, fieldWidth, 18);
        corner1Y = new GuiTextField(1, fontRenderer, startX + labelWidth, startY + 25, fieldWidth, 18);
        corner1Z = new GuiTextField(2, fontRenderer, startX + labelWidth, startY + 50, fieldWidth, 18);

        startX = centerX + spacing;
        corner2X = new GuiTextField(3, fontRenderer, startX + labelWidth, startY, fieldWidth, 18);
        corner2Y = new GuiTextField(4, fontRenderer, startX + labelWidth, startY + 25, fieldWidth, 18);
        corner2Z = new GuiTextField(5, fontRenderer, startX + labelWidth, startY + 50, fieldWidth, 18);

        GuiTextField[] fields = {corner1X, corner1Y, corner1Z, corner2X, corner2Y, corner2Z};
        for (GuiTextField field : fields) {
            field.setMaxStringLength(6);
            field.setText("0");
        }

        // Mode toggle button - outside the menu to the left
        modeToggleButton = new GuiButton(10, guiLeft - 65, guiTop + 10, 60, 20, "Mode: Save");
        buttonList.add(modeToggleButton);

        // Save mode elements
        int nameY = guiTop + 110;
        nameField = new GuiTextField(6, fontRenderer, guiLeft + 35, nameY + 3, 180, 18);
        nameField.setMaxStringLength(64);

        applyButton = new GuiButton(0, guiLeft + 20, guiTop + guiHeight - 35, 100, 20, "Apply");
        saveButton = new GuiButton(1, guiLeft + 136, guiTop + guiHeight - 35, 100, 20, "Save");

        // Smaller air toggle button
        airToggleButton = new GuiButton(2, guiLeft + 20, guiTop + guiHeight - 60, 100, 20, "Air: OFF");

        buttonList.add(airToggleButton);
        buttonList.add(applyButton);
        buttonList.add(saveButton);

        // Load mode elements
        nbtFileNameField = new GuiTextField(7, fontRenderer, guiLeft + 35, nameY + 3, 180, 18);
        nbtFileNameField.setMaxStringLength(64);

        // Load position fields - moved lower
        int loadPosYStart = guiTop + 70;
        loadPosX = new GuiTextField(15, fontRenderer, guiLeft + 30, loadPosYStart - 44, 50, 18);
        loadPosY = new GuiTextField(16, fontRenderer, guiLeft + 30, loadPosYStart - 18, 50, 18);
        loadPosZ = new GuiTextField(17, fontRenderer, guiLeft + 30, loadPosYStart + 8, 50, 18);
        loadPosX.setMaxStringLength(6);
        loadPosY.setMaxStringLength(6);
        loadPosZ.setMaxStringLength(6);

        // Load mode buttons
        readNbtButton = new GuiButton(11, guiLeft + 20, guiTop + guiHeight - 60, 100, 20, "Read .nbt");
        loadButton = new GuiButton(12, guiLeft + 136, guiTop + guiHeight - 35, 100, 20, "Load");
        visibilityToggleButton = new GuiButton(13, guiLeft + 20, guiTop + guiHeight - 35, 100, 20, "Preview: ON");
        rotateButton = new GuiButton(14, guiLeft + 136, guiTop + guiHeight - 60, 100, 20, "Rot: 0°");
        openFolderButton = new GuiButton(18, guiLeft + guiWidth - 70, guiTop + 5, 65, 20, "Structure Folder");
        buttonList.add(openFolderButton);

        previewX = guiLeft + guiWidth + 10;
        previewY = guiTop + 30;

        loadCoordinatesFromTile();
        updateModeVisibility();
    }

    private void updateModeVisibility() {
        if (isLoadMode) {
            // Hide save mode elements
            nameField.setVisible(false);
            applyButton.visible = false;
            saveButton.visible = false;
            airToggleButton.visible = false;
            corner1X.setVisible(false);
            corner1Y.setVisible(false);
            corner1Z.setVisible(false);
            corner2X.setVisible(false);
            corner2Y.setVisible(false);
            corner2Z.setVisible(false);

            // Show load mode elements
            if (!buttonList.contains(readNbtButton)) buttonList.add(readNbtButton);
            if (!buttonList.contains(loadButton)) buttonList.add(loadButton);
            if (!buttonList.contains(visibilityToggleButton)) buttonList.add(visibilityToggleButton);
            if (!buttonList.contains(rotateButton)) buttonList.add(rotateButton);

            openFolderButton.visible = true;
            readNbtButton.visible = true;
            loadButton.visible = true;
            visibilityToggleButton.visible = true;
            rotateButton.visible = true;
            nbtFileNameField.setVisible(true);
            loadPosX.setVisible(true);
            loadPosY.setVisible(true);
            loadPosZ.setVisible(true);

            modeToggleButton.displayString = "Mode: Load";
        } else {
            // Show save mode elements
            nameField.setVisible(true);
            applyButton.visible = true;
            saveButton.visible = true;
            airToggleButton.visible = true;
            corner1X.setVisible(true);
            corner1Y.setVisible(true);
            corner1Z.setVisible(true);
            corner2X.setVisible(true);
            corner2Y.setVisible(true);
            corner2Z.setVisible(true);

            // Hide load mode elements
            openFolderButton.visible = false;
            readNbtButton.visible = false;
            loadButton.visible = false;
            visibilityToggleButton.visible = false;
            rotateButton.visible = false;
            nbtFileNameField.setVisible(false);
            loadPosX.setVisible(false);
            loadPosY.setVisible(false);
            loadPosZ.setVisible(false);

            modeToggleButton.displayString = "Mode: Save";
        }
    }


    private void loadCoordinatesFromTile() {
        BlockPos pos1 = te.getCorner1();
        BlockPos pos2 = te.getCorner2();
        ignoreAir = te.getIgnoreAir();
        showPlacementPreview = te.getShowLoadPreview();
        rotation = te.getRotation();
        loadedStructureName = te.getStructureName();

        if (airToggleButton != null) {
            airToggleButton.displayString = "Air: " + (ignoreAir ? "ON" : "OFF");
        }
        if (visibilityToggleButton != null) {
            visibilityToggleButton.displayString = "Preview: " + (showPlacementPreview ? "ON" : "OFF");
        }
        if (rotateButton != null) {
            rotateButton.displayString = "Rot: " + rotation + "°";
        }

        if (pos1 != null) {
            corner1X.setText(String.valueOf(pos1.getX()));
            corner1Y.setText(String.valueOf(pos1.getY()));
            corner1Z.setText(String.valueOf(pos1.getZ()));
        }
        if (pos2 != null) {
            corner2X.setText(String.valueOf(pos2.getX()));
            corner2Y.setText(String.valueOf(pos2.getY()));
            corner2Z.setText(String.valueOf(pos2.getZ()));
        }
        if (nameField != null && te.getStructureName() != null) {
            nameField.setText(te.getStructureName());
        }

        // Load load position
        BlockPos loadPos = te.getLoadPosition();
        if (loadPos != null && !loadPos.equals(BlockPos.ORIGIN)) {
            loadPosition = loadPos;
            loadPosX.setText(String.valueOf(loadPos.getX()));
            loadPosY.setText(String.valueOf(loadPos.getY()));
            loadPosZ.setText(String.valueOf(loadPos.getZ()));
        } else {
            // Default: position above the structure block
            loadPosition = te.getPos().up();
            loadPosX.setText(String.valueOf(loadPosition.getX()));
            loadPosY.setText(String.valueOf(loadPosition.getY()));
            loadPosZ.setText(String.valueOf(loadPosition.getZ()));
        }
    }

    private void saveCoordinatesToTile() {
        try {
            int x1 = Integer.parseInt(corner1X.getText());
            int y1 = Integer.parseInt(corner1Y.getText());
            int z1 = Integer.parseInt(corner1Z.getText());
            int x2 = Integer.parseInt(corner2X.getText());
            int y2 = Integer.parseInt(corner2Y.getText());
            int z2 = Integer.parseInt(corner2Z.getText());

            BlockPos pos = te.getPos();
            Main.NETWORK.sendToServer(new PacketUpdateCorners(pos, new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2)));

            Main.NETWORK.sendToServer(new PacketUpdateLoadPosition(pos, BlockPos.ORIGIN));

            mc.player.closeScreen();
        } catch (NumberFormatException e) {}
    }

    private void saveStructure() {
        BlockPos pos = te.getPos();
        Main.NETWORK.sendToServer(new PacketSaveStructure(pos, nameField.getText()));
        mc.player.closeScreen();
    }

    private void loadStructure() {
        BlockPos pos = te.getPos();
        updateLoadPosition();
        Main.NETWORK.sendToServer(new PacketLoadStructure(pos, nbtFileNameField.getText(), rotation, showPlacementPreview));
        mc.player.closeScreen();
    }

    private void readNbtFile() {

        BlockPos pos = te.getPos();
        String fileName = nbtFileNameField.getText();

        if (fileName.isEmpty()) {
            return;
        }

        // Добавляем .nbt если нет расширения
        if (!fileName.endsWith(".nbt")) {
            fileName += ".nbt";
        }

        // Определяем путь к файлу
        File structuresDir;
        if (mc.isIntegratedServerRunning() && mc.getIntegratedServer() != null) {
            structuresDir = new File(mc.mcDataDir, "saves" + File.separator +
                    mc.getIntegratedServer().getFolderName() + File.separator + "structures");
        } else {
            structuresDir = new File(mc.mcDataDir, "structures");
        }

        File nbtFile = new File(structuresDir, fileName);

        if (!nbtFile.exists()) {
            return;
        }

        if (nbtFile.length() == 0) {
            return;
        }

        // Читаем NBT локально для GUI предпросмотра
        try {
            NBTTagCompound nbt = null;
            try {
                nbt = CompressedStreamTools.readCompressed(new FileInputStream(nbtFile));
            } catch (Exception e1) {
                try {
                    nbt = CompressedStreamTools.read(new DataInputStream(new FileInputStream(nbtFile)));
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }

            if (nbt != null) {
                loadedNbt = nbt;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        pos = te.getPos();
        fileName = nbtFileNameField.getText();

        Main.NETWORK.sendToServer(new PacketUpdateLoadPosition(pos, BlockPos.ORIGIN));

        Main.NETWORK.sendToServer(new PacketReadNbtFile(pos, fileName));
        loadedStructureName = fileName;
    }



    private void updateLoadPosition() {
        try {
            int x = Integer.parseInt(loadPosX.getText());
            int y = Integer.parseInt(loadPosY.getText());
            int z = Integer.parseInt(loadPosZ.getText());
            loadPosition = new BlockPos(x, y, z);
            Main.NETWORK.sendToServer(new PacketUpdateLoadPosition(te.getPos(), loadPosition));
        } catch (NumberFormatException e) {}
    }

    private void sendPreviewUpdate() {
        Main.NETWORK.sendToServer(new PacketUpdatePreview(te.getPos(), showPlacementPreview));
    }

    private void sendRotationUpdate() {
        Main.NETWORK.sendToServer(new PacketUpdateRotation(te.getPos(), rotation));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == applyButton) saveCoordinatesToTile();
        if (button == saveButton) saveStructure();
        if (button == loadButton) loadStructure();
        if (button == readNbtButton) readNbtFile();
        if (button == modeToggleButton) {
            isLoadMode = !isLoadMode;
            updateModeVisibility();
        }
        if (button == airToggleButton) {
            ignoreAir = !ignoreAir;
            airToggleButton.displayString = "Air: " + (ignoreAir ? "ON" : "OFF");
            Main.NETWORK.sendToServer(new PacketToggleIgnoreAir(te.getPos(), ignoreAir));
        }
        if (button == visibilityToggleButton) {
            showPlacementPreview = !showPlacementPreview;
            visibilityToggleButton.displayString = "Preview: " + (showPlacementPreview ? "ON" : "OFF");
            sendPreviewUpdate();
        }
        if (button == rotateButton) {
            rotation = (rotation + 90) % 360;
            rotateButton.displayString = "Rot: " + rotation + "°";
            sendRotationUpdate();
        }
        if (button == openFolderButton) {
            File structuresDir = new File(mc.getIntegratedServer().getFolderName() != null
                    ? mc.mcDataDir + File.separator + "saves" + File.separator + mc.getIntegratedServer().getFolderName() + File.separator + "structures"
                    : mc.mcDataDir + File.separator + "structures");
            if (!structuresDir.exists()) {
                structuresDir.mkdirs();
            }
            try {
                Desktop.getDesktop().open(structuresDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // Fix: Only process input for visible and focused fields
        if (isLoadMode) {
            // In load mode, only process load mode fields
            if (nbtFileNameField.isFocused()) {
                nbtFileNameField.textboxKeyTyped(typedChar, keyCode);
            } else if (loadPosX.isFocused()) {
                loadPosX.textboxKeyTyped(typedChar, keyCode);
                updateLoadPosition();
            } else if (loadPosY.isFocused()) {
                loadPosY.textboxKeyTyped(typedChar, keyCode);
                updateLoadPosition();
            } else if (loadPosZ.isFocused()) {
                loadPosZ.textboxKeyTyped(typedChar, keyCode);
                updateLoadPosition();
            }
        } else {
            // In save mode, only process save mode fields
            if (nameField.isFocused()) {
                nameField.textboxKeyTyped(typedChar, keyCode);
            } else {
                corner1X.textboxKeyTyped(typedChar, keyCode);
                corner1Y.textboxKeyTyped(typedChar, keyCode);
                corner1Z.textboxKeyTyped(typedChar, keyCode);
                corner2X.textboxKeyTyped(typedChar, keyCode);
                corner2Y.textboxKeyTyped(typedChar, keyCode);
                corner2Z.textboxKeyTyped(typedChar, keyCode);
            }
        }
        if (keyCode == 1) {
            // Save state before closing
            saveGuiState();
            mc.player.closeScreen();
        }
    }

    private void saveGuiState() {
        if (isLoadMode) {
            updateLoadPosition();
            sendPreviewUpdate();
            sendRotationUpdate();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        saveGuiState();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int mouseX = org.lwjgl.input.Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - org.lwjgl.input.Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        if (mouseX >= previewX && mouseX <= previewX + PREVIEW_SIZE
                && mouseY >= previewY && mouseY <= previewY + PREVIEW_SIZE) {

            int dWheel = org.lwjgl.input.Mouse.getEventDWheel();
            if (dWheel != 0) {
                zoomLevel += dWheel > 0 ? 0.1F : -0.1F;
                zoomLevel = Math.max(0.2F, Math.min(5.0F, zoomLevel));
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0 && mouseX >= previewX && mouseX <= previewX + PREVIEW_SIZE
                && mouseY >= previewY && mouseY <= previewY + PREVIEW_SIZE) {
            return;
        }

        // Only process clicks for visible elements
        if (isLoadMode) {
            nbtFileNameField.mouseClicked(mouseX, mouseY, mouseButton);
            loadPosX.mouseClicked(mouseX, mouseY, mouseButton);
            loadPosY.mouseClicked(mouseX, mouseY, mouseButton);
            loadPosZ.mouseClicked(mouseX, mouseY, mouseButton);
        } else {
            corner1X.mouseClicked(mouseX, mouseY, mouseButton);
            corner1Y.mouseClicked(mouseX, mouseY, mouseButton);
            corner1Z.mouseClicked(mouseX, mouseY, mouseButton);
            corner2X.mouseClicked(mouseX, mouseY, mouseButton);
            corner2Y.mouseClicked(mouseX, mouseY, mouseButton);
            corner2Z.mouseClicked(mouseX, mouseY, mouseButton);
            nameField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void updateScreen() {
        if (isLoadMode) {
            nbtFileNameField.updateCursorCounter();
            loadPosX.updateCursorCounter();
            loadPosY.updateCursorCounter();
            loadPosZ.updateCursorCounter();
        } else {
            corner1X.updateCursorCounter();
            corner1Y.updateCursorCounter();
            corner1Z.updateCursorCounter();
            corner2X.updateCursorCounter();
            corner2Y.updateCursorCounter();
            corner2Z.updateCursorCounter();
            nameField.updateCursorCounter();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Only draw save mode elements if in save mode
        if (!isLoadMode) {
            corner1X.drawTextBox();
            corner1Y.drawTextBox();
            corner1Z.drawTextBox();
            corner2X.drawTextBox();
            corner2Y.drawTextBox();
            corner2Z.drawTextBox();
            nameField.drawTextBox();

            int centerX = guiLeft + guiWidth / 2;
            int startY = guiTop + 30;
            int labelWidth = 40;
            int spacing = 10;
            int totalWidth = labelWidth + 50;
            int leftX = centerX - totalWidth - spacing;
            int rightX = centerX + spacing;

            fontRenderer.drawString("X:", leftX, startY + 6, 0xFFFFFF);
            fontRenderer.drawString("Y:", leftX, startY + 31, 0xFFFFFF);
            fontRenderer.drawString("Z:", leftX, startY + 56, 0xFFFFFF);
            fontRenderer.drawString("Pos1", leftX, startY - 12, 0xFFAA00);

            fontRenderer.drawString("X:", rightX, startY + 6, 0xFFFFFF);
            fontRenderer.drawString("Y:", rightX, startY + 31, 0xFFFFFF);
            fontRenderer.drawString("Z:", rightX, startY + 56, 0xFFFFFF);
            fontRenderer.drawString("Pos2", rightX, startY - 12, 0xFFAA00);
        }

        int nameY = guiTop + 110;
        fontRenderer.drawString("NBT File:", guiLeft, nameY + 8, 0xFFFFFF);

        if (isLoadMode) {
            nbtFileNameField.drawTextBox();
            loadPosX.drawTextBox();
            loadPosY.drawTextBox();
            loadPosZ.drawTextBox();

            fontRenderer.drawString("X:", guiLeft + 20, guiTop + 6 + 25, 0xFFFFFF);
            fontRenderer.drawString("Y:", guiLeft + 20, guiTop + 31+ 25, 0xFFFFFF);
            fontRenderer.drawString("Z:", guiLeft + 20, guiTop + 56+ 25, 0xFFFFFF);

            // Draw loaded structure name
            if (!loadedStructureName.isEmpty()) {
                String labelText = "Loaded: " + loadedStructureName;
                int labelWidth = fontRenderer.getStringWidth(labelText);
                fontRenderer.drawString(labelText, guiLeft + (guiWidth - labelWidth) / 2, guiTop + 95, 0x00FF00);
            }
        }
        BlockPreviewRenderer.drawBlockPreview(te, loadedNbt, previewX, previewY, guiWidth, guiHeight, zoomLevel, mc, fontRenderer);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // Only draw the title and separator lines, no background box
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        String title = "Structure Block";
        int titleWidth = fontRenderer.getStringWidth(title);
        fontRenderer.drawString(title, guiLeft + (guiWidth - titleWidth) / 2, guiTop + 8, 0x00AAFF);

        int centerX = guiLeft + guiWidth / 2;
        drawVerticalLine(centerX, guiTop + 25, guiTop + 100, 0xFF444444);
        drawHorizontalLine(guiLeft + 5, guiLeft + guiWidth - 5, guiTop + 105, 0xFF444444);


        GlStateManager.popMatrix();
    }
}