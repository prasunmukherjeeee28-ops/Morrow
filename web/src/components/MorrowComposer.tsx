import type { KeyboardEvent } from 'react';
import { useEffect, useRef } from 'react';
import { ArrowUp, Sparkles } from 'lucide-react';

type Props = {
  value: string;
  busy: boolean;
  onChange: (value: string) => void;
  onSend: () => void;
};

export default function MorrowComposer({ value, busy, onChange, onSend }: Props) {
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    const element = textareaRef.current;
    if (!element) return;
    element.style.height = '0px';
    element.style.height = Math.min(element.scrollHeight, 132) + 'px';
  }, [value]);

  function handleKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === 'Enter' && !event.shiftKey && !event.nativeEvent.isComposing) {
      event.preventDefault();
      onSend();
    }
  }

  return (
    <div className="composer-wrap">
      <div className="composer">
        <div className="composer-prompt">
          <Sparkles size={15} strokeWidth={1.7} />
          <span>MESSAGE MORROW</span>
        </div>
        <textarea
          ref={textareaRef}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ask anything. Start anywhere..."
          rows={1}
          aria-label="Message Morrow"
          disabled={busy}
        />
        <div className="composer-footer">
          <span className="composer-hint">Enter to send <span>·</span> Shift + Enter for a new line</span>
          <button
            className="send-button"
            onClick={onSend}
            disabled={!value.trim() || busy}
            aria-label="Send message"
            title="Send message"
          >
            <ArrowUp size={19} strokeWidth={2.2} />
          </button>
        </div>
      </div>
      <p className="fineprint">Morrow can make mistakes. Check important information.</p>
    </div>
  );
}
