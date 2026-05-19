package sgc;


import net.minecraft.creativetab.*;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import sgc.supporting.pakets.*;
import sgc.supporting.proxy.CommonProxy;

@Mod(modid = Main.MODID, name = Main.NAME, version = Main.VERSION, acceptedMinecraftVersions = Main.MC_VERSION)
public class Main {
	public static final String MODID = "sgc";
	public static final String NAME = "StructureGC API";
	public static final String VERSION = "0.1";
	public static final String MC_VERSION = "[1.12.2]";

	public static final String CLIENT = MODID + ".supporting.proxy.ClientProxy";
	public static final String SERVER = MODID + ".supporting.proxy.CommonProxy";

	public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

	public static final CreativeTabs CT_MOD = new CreativeTabs(Main.MODID) { @Override public ItemStack getTabIconItem() { return new ItemStack(Items.BOOK); } };

	@Instance
	public static Main instance;
	@SidedProxy(clientSide = CLIENT, serverSide = SERVER)
	public static CommonProxy proxy;

	@EventHandler public static void preInit(FMLPreInitializationEvent e) {
		NETWORK.registerMessage(PacketUpdateCorners.Handler.class, PacketUpdateCorners.class, 0, Side.SERVER);
		NETWORK.registerMessage(PacketSaveStructure.Handler.class, PacketSaveStructure.class, 1, Side.SERVER);
		NETWORK.registerMessage(PacketToggleIgnoreAir.Handler.class, PacketToggleIgnoreAir.class, 2, Side.SERVER);
		NETWORK.registerMessage(PacketReadNbtFile.Handler.class, PacketReadNbtFile.class, 3, Side.SERVER);
		NETWORK.registerMessage(PacketLoadStructure.Handler.class, PacketLoadStructure.class, 4, Side.SERVER);
		NETWORK.registerMessage(PacketUpdateLoadPosition.Handler.class, PacketUpdateLoadPosition.class, 5, Side.SERVER);
		NETWORK.registerMessage(PacketUpdatePreview.Handler.class, PacketUpdatePreview.class, 6, Side.SERVER);
		NETWORK.registerMessage(PacketUpdateRotation.Handler.class, PacketUpdateRotation.class, 7, Side.SERVER);
		proxy.preInit(e);
	}
	@EventHandler public static void init(FMLInitializationEvent e) {
		proxy.init(e);
	}
	@EventHandler public static void postInit(FMLPostInitializationEvent e) {
		proxy.postInit(e);
	}
}
