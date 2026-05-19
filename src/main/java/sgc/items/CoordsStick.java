package sgc.items;

import sgc.api.SelectionTool;

public class CoordsStick extends SelectionTool {
    public CoordsStick(String name) {
        super(name, 1);
        setMaxStackSize(1);
    }
}