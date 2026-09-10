package hk.quantr.riscvstudio.asm;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

@MimeRegistration(mimeType = RiscvAsmMime.MIME, service = HighlightsLayerFactory.class)
public class RiscvHighlightsFactory implements HighlightsLayerFactory {

	@Override
	public HighlightsLayer[] createLayers(Context context) {
		RiscvSyntaxHighlights highlights = new RiscvSyntaxHighlights(context.getDocument());
		return new HighlightsLayer[]{
			HighlightsLayer.create(
					RiscvAsmMime.MIME + "-syntax",
					ZOrder.SYNTAX_RACK,
					true,
					highlights.getBag()
			)
		};
	}
}
