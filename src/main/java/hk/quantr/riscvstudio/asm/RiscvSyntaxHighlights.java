package hk.quantr.riscvstudio.asm;

import hk.quantr.assembler.antlr.RISCVAssemblerLexer;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Colors {@code .asm} using {@code RISCVAssemblerLexer.g4}. A second pass applies
 * {@code RISCVAssemblerParser.g4} rules {@code section} ({@code DOT IDENTIFIER})
 * and {@code label} ({@code IDENTIFIER COLON}).
 */
final class RiscvSyntaxHighlights implements DocumentListener {

	private static final RequestProcessor RP = new RequestProcessor(RiscvSyntaxHighlights.class);
	private static final Map<String, AttributeSet> FALLBACK = fallbackColors();

	private final Document document;
	private final OffsetsBag bag;
	private final RequestProcessor.Task task;

	RiscvSyntaxHighlights(Document document) {
		this.document = document;
		this.bag = new OffsetsBag(document);
		this.task = RP.create(this::recolor);
		document.putProperty(RiscvSyntaxHighlights.class, this);
		document.addDocumentListener(WeakListeners.document(this, document));
		task.schedule(0);
	}

	OffsetsBag getBag() {
		return bag;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		task.schedule(80);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		task.schedule(80);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	private void recolor() {
		String text;
		try {
			text = document.getText(0, document.getLength());
		} catch (BadLocationException ex) {
			return;
		}
		List<Span> spans = colorize(text);
		FontColorSettings colors = MimeLookup.getLookup(RiscvAsmMime.MIME).lookup(FontColorSettings.class);
		EventQueue.invokeLater(() -> apply(spans, colors));
	}

	private void apply(List<Span> spans, FontColorSettings colors) {
		OffsetsBag next = new OffsetsBag(document);
		for (Span span : spans) {
			if (span.end <= span.start) {
				continue;
			}
			AttributeSet attributes = attributes(colors, span.category);
			if (attributes != null) {
				next.addHighlight(span.start, span.end, attributes);
			}
		}
		bag.setHighlights(next);
		next.clear();
	}

	static List<Span> colorize(String text) {
		RISCVAssemblerLexer lexer = new RISCVAssemblerLexer(CharStreams.fromString(text));
		lexer.removeErrorListeners();
		List<Raw> tokens = new ArrayList<>();
		for (Token token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken()) {
			if (token.getChannel() != Token.DEFAULT_CHANNEL && token.getType() != RISCVAssemblerLexer.LINE_COMMENT) {
				if (token.getType() == RISCVAssemblerLexer.WS) {
					continue;
				}
			}
			if (token.getType() == RISCVAssemblerLexer.WS) {
				continue;
			}
			tokens.add(new Raw(token.getType(), token.getStartIndex(), token.getStopIndex() + 1));
		}
		List<Span> spans = new ArrayList<>(tokens.size());
		for (int i = 0; i < tokens.size(); i++) {
			Raw token = tokens.get(i);
			String category = RiscvTokenCategories.category(token.type);
			Raw next = i + 1 < tokens.size() ? tokens.get(i + 1) : null;
			if (token.type == RISCVAssemblerLexer.DOT && next != null && next.type == RISCVAssemblerLexer.IDENTIFIER) {
				category = RiscvTokenCategories.DIRECTIVE;
			} else if (token.type == RISCVAssemblerLexer.IDENTIFIER && next != null && next.type == RISCVAssemblerLexer.COLON) {
				category = RiscvTokenCategories.LABEL;
			} else if (token.type == RISCVAssemblerLexer.IDENTIFIER) {
				Raw prev = i > 0 ? tokens.get(i - 1) : null;
				if (prev != null && prev.type == RISCVAssemblerLexer.DOT) {
					category = RiscvTokenCategories.DIRECTIVE;
				}
			}
			if (category != null) {
				spans.add(new Span(token.start, token.end, category));
			}
		}
		return spans;
	}

	private static AttributeSet attributes(FontColorSettings colors, String category) {
		if (colors != null) {
			AttributeSet set = colors.getTokenFontColors(category);
			if (set != null) {
				return set;
			}
		}
		return FALLBACK.get(category);
	}

	private static Map<String, AttributeSet> fallbackColors() {
		Map<String, AttributeSet> map = new HashMap<>();
		map.put(RiscvTokenCategories.KEYWORD, color(0x15, 0x65, 0xC0, true));
		map.put(RiscvTokenCategories.REGISTER, color(0x00, 0x89, 0x7B, false));
		map.put(RiscvTokenCategories.DIRECTIVE, color(0x6A, 0x1B, 0x9A, true));
		map.put(RiscvTokenCategories.LABEL, color(0xEF, 0x6C, 0x00, true));
		map.put(RiscvTokenCategories.IDENTIFIER, color(0x37, 0x47, 0x4F, false));
		map.put(RiscvTokenCategories.NUMBER, color(0x2E, 0x7D, 0x32, false));
		map.put(RiscvTokenCategories.COMMENT, color(0x75, 0x75, 0x75, false));
		map.put(RiscvTokenCategories.STRING, color(0xC6, 0x28, 0x28, false));
		map.put(RiscvTokenCategories.OPERATOR, color(0x54, 0x6E, 0x7A, false));
		map.put(RiscvTokenCategories.ERROR, color(0xB7, 0x1C, 0x1C, false));
		return map;
	}

	private static AttributeSet color(int r, int g, int b, boolean bold) {
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, new Color(r, g, b));
		if (bold) {
			StyleConstants.setBold(set, true);
		}
		return AttributesUtilities.createImmutable(set);
	}

	private record Raw(int type, int start, int end) {
	}

	record Span(int start, int end, String category) {
	}
}
