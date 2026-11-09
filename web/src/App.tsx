import { useEffect, useMemo, useRef, useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import {
  ArrowUpRight,
  BookOpen,
  Check,
  Code2,
  Compass,
  Copy,
  Menu,
  Plus,
  RefreshCw,
  Sparkles,
} from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import MorrowComposer from './components/MorrowComposer';
import MorrowOrb from './components/MorrowOrb';
import MorrowSidebar, { type ConversationPreview } from './components/MorrowSidebar';

type ChatMessage = {
  id: string;
  role: 'user' | 'assistant';
  text: string;
  createdAt: number;
};

type Conversation = ConversationPreview & {
  messages: ChatMessage[];
};

type StoredState = {
  conversations: Conversation[];
  activeId: string | null;
};

declare global {
  interface Window {
    MorrowBridge?: {
      openClassic: () => void;
      sendMessage?: (requestId: string, message: string) => void;
    };
  }
}

const CHAT_ENDPOINT = 'https://morrow-backend.prasunmukherjejee28.workers.dev/chat';
const STORAGE_KEY = 'morrow.react.workspace.v1';

function makeId() {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) return crypto.randomUUID();
  return Date.now().toString(36) + Math.random().toString(36).slice(2);
}

function loadState(): StoredState {
  try {
    const parsed = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}') as Partial<StoredState>;
    const conversations = Array.isArray(parsed.conversations) ? parsed.conversations : [];
    const activeId = conversations.some((item) => item.id === parsed.activeId) ? parsed.activeId ?? null : null;
    return { conversations, activeId };
  } catch {
    return { conversations: [], activeId: null };
  }
}

function makeTitle(text: string) {
  const cleaned = text.replace(/\s+/g, ' ').trim();
  return cleaned.length > 38 ? cleaned.slice(0, 38).trimEnd() + '…' : cleaned || 'New conversation';
}

function updateConversation(
  state: StoredState,
  id: string,
  transform: (conversation: Conversation) => Conversation,
): StoredState {
  const index = state.conversations.findIndex((conversation) => conversation.id === id);
  if (index < 0) return state;
  const updated = transform(state.conversations[index]);
  const conversations = [updated, ...state.conversations.filter((conversation) => conversation.id !== id)]
    .sort((a, b) => b.updatedAt - a.updatedAt);
  return { ...state, conversations, activeId: id };
}

function AssistantMark() {
  return <div className="assistant-mark" aria-hidden="true"><span>m</span></div>;
}

const suggestions = [
  { icon: Compass, title: 'Explore an idea', detail: 'Turn a passing thought into something bigger.', prompt: 'Help me explore an interesting idea.' },
  { icon: Code2, title: 'Build something', detail: 'Plan a project and work through it step by step.', prompt: 'Help me plan and build a small project.' },
  { icon: BookOpen, title: 'Learn something', detail: 'Make a difficult topic feel understandable.', prompt: 'Teach me a difficult topic in a simple way.' },
];

export default function App() {
  const [stored, setStored] = useState<StoredState>(loadState);
  const [input, setInput] = useState('');
  const [busy, setBusy] = useState(false);
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const listRef = useRef<HTMLDivElement>(null);
  const activeConversation = useMemo(
    () => stored.conversations.find((conversation) => conversation.id === stored.activeId) ?? null,
    [stored.conversations, stored.activeId],
  );
  const messages = activeConversation?.messages ?? [];
  const hasNativeBridge = typeof window !== 'undefined' && Boolean(window.MorrowBridge);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(stored));
    } catch {
      // Keep the current chat usable if local storage is unavailable.
    }
  }, [stored]);

  useEffect(() => {
    listRef.current?.scrollTo({ top: listRef.current.scrollHeight, behavior: 'smooth' });
  }, [messages.length, busy, stored.activeId]);

  function startNewChat() {
    setStored((current) => ({ ...current, activeId: null }));
    setInput('');
  }

  function addUserMessage(text: string) {
    const id = stored.activeId || makeId();
    const message: ChatMessage = { id: makeId(), role: 'user', text, createdAt: Date.now() };
    setStored((current) => {
      const existing = current.conversations.find((conversation) => conversation.id === id);
      const conversation: Conversation = existing
        ? { ...existing, messages: [...existing.messages, message], updatedAt: Date.now() }
        : { id, title: makeTitle(text), updatedAt: Date.now(), messages: [message] };
      return {
        conversations: [conversation, ...current.conversations.filter((item) => item.id !== id)]
          .sort((a, b) => b.updatedAt - a.updatedAt)
          .slice(0, 60),
        activeId: id,
      };
    });
    return id;
  }

  function appendAssistantMessage(conversationId: string, text: string) {
    const message: ChatMessage = { id: makeId(), role: 'assistant', text, createdAt: Date.now() };
    setStored((current) => updateConversation(current, conversationId, (conversation) => ({
      ...conversation,
      messages: [...conversation.messages, message],
      updatedAt: Date.now(),
    })));
  }

  async function getMorrowReply(prompt: string): Promise<string> {
    if (window.MorrowBridge?.sendMessage) {
      return new Promise((resolve, reject) => {
        const requestId = makeId();
        const cleanup = () => {
          window.clearTimeout(timeoutId);
          window.removeEventListener('morrow-native-reply', handleReply);
        };
        const handleReply = (event: Event) => {
          const detail = (event as CustomEvent<{ requestId: string; reply?: string; error?: string }>).detail;
          if (detail?.requestId !== requestId) return;
          cleanup();
          if (detail.error) reject(new Error(detail.error));
          else if (detail.reply) resolve(detail.reply);
          else reject(new Error('Morrow returned an empty response.'));
        };
        const timeoutId = window.setTimeout(() => {
          cleanup();
          reject(new Error('The request timed out. Please try again.'));
        }, 70000);
        window.addEventListener('morrow-native-reply', handleReply);
        try {
          window.MorrowBridge?.sendMessage?.(requestId, prompt);
        } catch (error) {
          cleanup();
          reject(error instanceof Error ? error : new Error('Could not start the native request.'));
        }
      });
    }

    const response = await fetch(CHAT_ENDPOINT, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: prompt }),
    });
    const raw = await response.text();
    let data: Record<string, unknown> = {};
    try {
      data = JSON.parse(raw) as Record<string, unknown>;
    } catch {
      if (!response.ok) throw new Error(raw || 'The AI service returned an error.');
      return raw || 'Morrow did not return a response.';
    }
    if (!response.ok) {
      const reason = data.detail ?? data.error ?? data.message;
      throw new Error(typeof reason === 'string' ? reason : 'The AI service could not complete the request.');
    }
    const choice = data.choices as Array<{ message?: { content?: string } }> | undefined;
    const answer = data.reply ?? data.response ?? data.message ?? choice?.[0]?.message?.content;
    return typeof answer === 'string' ? answer : 'Morrow did not return a readable response.';
  }

  async function requestAnswer(conversationId: string, prompt: string) {
    setBusy(true);
    try {
      const answer = await getMorrowReply(prompt);
      appendAssistantMessage(conversationId, answer);
    } catch (error) {
      const reason = error instanceof Error ? error.message : 'Unknown connection error.';
      appendAssistantMessage(
        conversationId,
        'I could not reach the Morrow AI service. ' + reason + '\n\nPlease check the connection and try again.',
      );
    } finally {
      setBusy(false);
    }
  }

  async function sendMessage() {
    const text = input.trim();
    if (!text || busy) return;
    setInput('');
    const conversationId = addUserMessage(text);
    await requestAnswer(conversationId, text);
  }

  function selectConversation(id: string) {
    if (busy) return;
    setStored((current) => ({ ...current, activeId: id }));
    setInput('');
  }

  async function retryLastAnswer() {
    if (!activeConversation || busy) return;
    const lastUserIndex = activeConversation.messages.map((message) => message.role).lastIndexOf('user');
    if (lastUserIndex < 0) return;
    const lastUser = activeConversation.messages[lastUserIndex];
    setStored((current) => updateConversation(current, activeConversation.id, (conversation) => ({
      ...conversation,
      messages: conversation.messages.slice(0, lastUserIndex + 1),
    })));
    await requestAnswer(activeConversation.id, lastUser.text);
  }

  async function copyMessage(message: ChatMessage) {
    try {
      await navigator.clipboard.writeText(message.text);
      setCopiedId(message.id);
      window.setTimeout(() => setCopiedId((current) => current === message.id ? null : current), 1400);
    } catch {
      setCopiedId(null);
    }
  }

  return (
    <div className="app-shell">
      <div className="ambient-glow ambient-one" />
      <div className="ambient-glow ambient-two" />

      <MorrowSidebar
        conversations={stored.conversations}
        activeId={stored.activeId}
        mobileOpen={mobileSidebarOpen}
        hasNativeBridge={hasNativeBridge}
        onNewChat={startNewChat}
        onSelect={selectConversation}
        onClose={() => setMobileSidebarOpen(false)}
        onOpenClassic={() => window.MorrowBridge?.openClassic()}
      />

      <AnimatePresence>
        {mobileSidebarOpen && (
          <motion.button
            className="sidebar-scrim"
            aria-label="Close navigation"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setMobileSidebarOpen(false)}
          />
        )}
      </AnimatePresence>

      <main className="main-panel">
        <header className="topbar">
          <div className="topbar-left">
            <button className="icon-button mobile-menu" onClick={() => setMobileSidebarOpen(true)} aria-label="Open navigation">
              <Menu size={19} />
            </button>
            <div className="topbar-title">
              <span className="topbar-title-main">Morrow</span>
              <span className="topbar-divider" />
              <span className="topbar-subtitle">{activeConversation?.title || 'A space to think'}</span>
            </div>
          </div>
          <div className="topbar-right">
            <div className="engine-pill"><span /> MORROW CORE</div>
            <button className="icon-button topbar-new" onClick={startNewChat} aria-label="Start a new conversation" title="New conversation">
              <Plus size={18} strokeWidth={1.8} />
            </button>
          </div>
        </header>

        <div className="workspace">
          {messages.length === 0 ? (
            <section className="welcome-screen">
              <motion.div
                className="welcome-orb"
                initial={{ opacity: 0, scale: 0.88, y: 12 }}
                animate={{ opacity: 1, scale: 1, y: 0 }}
                transition={{ duration: 0.8, ease: [0.22, 1, 0.36, 1] }}
              >
                <MorrowOrb />
              </motion.div>
              <motion.div
                className="welcome-copy"
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.65, delay: 0.1 }}
              >
                <p className="eyebrow"><Sparkles size={13} /> YOUR SPACE TO THINK</p>
                <h1>Make room for<br /><span>better questions.</span></h1>
                <p className="welcome-description">Ideas, questions, projects, late-night curiosity. Start wherever your mind takes you.</p>
              </motion.div>
              <div className="suggestion-grid">
                {suggestions.map((item, index) => {
                  const Icon = item.icon;
                  return (
                    <motion.button
                      className="suggestion-card"
                      key={item.title}
                      onClick={() => setInput(item.prompt)}
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.4, delay: 0.2 + index * 0.08 }}
                      whileTap={{ scale: 0.985 }}
                    >
                      <span className="suggestion-icon"><Icon size={17} strokeWidth={1.8} /></span>
                      <span className="suggestion-text">
                        <strong>{item.title}</strong>
                        <small>{item.detail}</small>
                      </span>
                      <ArrowUpRight className="suggestion-arrow" size={16} />
                    </motion.button>
                  );
                })}
              </div>
            </section>
          ) : (
            <section className="conversation-screen">
              <div className="conversation-scroll" ref={listRef}>
                <div className="conversation-inner">
                  <div className="conversation-heading">
                    <span className="conversation-kicker">CONVERSATION</span>
                    <h1>{activeConversation?.title || 'New conversation'}</h1>
                  </div>
                  {messages.map((message, index) => (
                    <motion.article
                      className={'message-row ' + message.role}
                      key={message.id}
                      initial={{ opacity: 0, y: 9 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.25 }}
                    >
                      {message.role === 'assistant' ? <AssistantMark /> : <div className="user-mark">Y</div>}
                      <div className="message-body">
                        <div className="message-label">{message.role === 'assistant' ? 'MORROW' : 'YOU'}</div>
                        <div className="message-content">
                          {message.role === 'assistant'
                            ? <ReactMarkdown remarkPlugins={[remarkGfm]}>{message.text}</ReactMarkdown>
                            : <p>{message.text}</p>}
                        </div>
                        {message.role === 'assistant' && (
                          <div className="message-actions">
                            <button className="message-action" onClick={() => copyMessage(message)} aria-label="Copy response" title="Copy response">
                              {copiedId === message.id ? <Check size={14} /> : <Copy size={14} />}
                              <span>{copiedId === message.id ? 'Copied' : 'Copy'}</span>
                            </button>
                            {index === messages.length - 1 && (
                              <button className="message-action" onClick={retryLastAnswer} disabled={busy} aria-label="Regenerate response" title="Regenerate response">
                                <RefreshCw size={14} /><span>Regenerate</span>
                              </button>
                            )}
                          </div>
                        )}
                      </div>
                    </motion.article>
                  ))}
                  {busy && (
                    <div className="message-row assistant thinking-row">
                      <AssistantMark />
                      <div className="thinking-copy"><span /> <span /> <span /> <small>Morrow is thinking</small></div>
                    </div>
                  )}
                  <div className="scroll-anchor" />
                </div>
              </div>
            </section>
          )}
        </div>

        <footer className="composer-dock">
          <MorrowComposer value={input} busy={busy} onChange={setInput} onSend={sendMessage} />
        </footer>
      </main>
    </div>
  );
}

