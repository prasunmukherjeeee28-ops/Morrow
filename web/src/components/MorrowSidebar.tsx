import { Clock3, MessageSquare, Plus, Sparkles, X } from 'lucide-react';

export type ConversationPreview = {
  id: string;
  title: string;
  updatedAt: number;
};

type Props = {
  conversations: ConversationPreview[];
  activeId: string | null;
  mobileOpen: boolean;
  hasNativeBridge: boolean;
  onNewChat: () => void;
  onSelect: (id: string) => void;
  onClose: () => void;
  onOpenClassic: () => void;
};

export default function MorrowSidebar({
  conversations,
  activeId,
  mobileOpen,
  hasNativeBridge,
  onNewChat,
  onSelect,
  onClose,
  onOpenClassic,
}: Props) {
  return (
    <aside className={'sidebar' + (mobileOpen ? ' sidebar-open' : '')}>
      <div className="sidebar-top">
        <div className="brand-lockup">
          <div className="brand-symbol"><span /></div>
          <div className="brand-name">morrow<span>✳</span></div>
        </div>
        <button className="icon-button sidebar-close" onClick={onClose} aria-label="Close menu">
          <X size={18} />
        </button>
      </div>

      <button className="new-chat-button" onClick={() => { onNewChat(); onClose(); }}>
        <Plus size={17} strokeWidth={2.1} />
        <span>New conversation</span>
        <kbd>⌘ K</kbd>
      </button>

      <div className="sidebar-section">
        <div className="section-label"><Clock3 size={13} /> RECENT</div>
        <div className="history-list">
          {conversations.length === 0 ? (
            <div className="history-empty">Your conversations will appear here.</div>
          ) : conversations.map((conversation) => (
            <button
              className={'history-item' + (activeId === conversation.id ? ' selected' : '')}
              key={conversation.id}
              onClick={() => { onSelect(conversation.id); onClose(); }}
              title={conversation.title}
            >
              <MessageSquare size={15} />
              <span>{conversation.title}</span>
            </button>
          ))}
        </div>
      </div>

      <div className="sidebar-bottom">
        {hasNativeBridge && (
          <button className="classic-link" onClick={onOpenClassic}>
            <Sparkles size={15} />
            <span>Open classic Morrow</span>
          </button>
        )}
        <div className="profile-card">
          <div className="profile-avatar">M</div>
          <div className="profile-copy">
            <strong>Morrow</strong>
            <span>Personal workspace</span>
          </div>
          <span className="profile-presence" />
        </div>
        <p className="sidebar-version">MORROW · BUILT TO THINK WITH YOU</p>
      </div>
    </aside>
  );
}
