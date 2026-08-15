"use client";

import { useCallback, useEffect, useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import styles from "./dashboard.module.css";

export type PrototypeVariant = "A" | "B" | "C";

const variantMeta: Record<PrototypeVariant, { name: string; question: string }> = {
  A: {
    name: "工程田野日志",
    question: "时间能否成为知识工作台的主导航？",
  },
  B: {
    name: "知识星图",
    question: "项目关系能否比模块菜单更适合恢复上下文？",
  },
  C: {
    name: "命令账本",
    question: "高密度与键盘优先能否成为产品识别度？",
  },
};

const variants: PrototypeVariant[] = ["A", "B", "C"];

function Icon({ name }: { name: string }) {
  const paths: Record<string, React.ReactNode> = {
    search: <><circle cx="11" cy="11" r="6"/><path d="m16 16 4 4"/></>,
    plus: <><path d="M12 5v14M5 12h14"/></>,
    note: <><path d="M6 3h9l3 3v15H6z"/><path d="M14 3v4h4M9 11h6M9 15h6"/></>,
    task: <><rect x="4" y="4" width="16" height="16" rx="2"/><path d="m8 12 3 3 5-6"/></>,
    link: <><path d="M10 13a5 5 0 0 0 7.5.5l2-2a5 5 0 0 0-7-7l-1.1 1"/><path d="M14 11a5 5 0 0 0-7.5-.5l-2 2a5 5 0 0 0 7 7l1.1-1"/></>,
    project: <><path d="M3 7h7l2 2h9v11H3z"/><path d="M3 7V4h7l2 3"/></>,
    arrow: <path d="m9 18 6-6-6-6"/>,
    spark: <><path d="m12 2 1.7 5.3L19 9l-5.3 1.7L12 16l-1.7-5.3L5 9l5.3-1.7z"/><path d="m19 16 .7 2.3L22 19l-2.3.7L19 22l-.7-2.3L16 19l2.3-.7z"/></>,
  };
  return (
    <svg className={styles.icon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      {paths[name]}
    </svg>
  );
}

function FieldLog() {
  const [done, setDone] = useState(false);
  return (
    <main className={`${styles.surface} ${styles.fieldLog}`}>
      <aside className={styles.fieldRail}>
        <div className={styles.fieldMark}>MF<span>25</span></div>
        <nav aria-label="主导航">
          <a className={styles.fieldNavActive}>今日</a>
          <a>项目</a><a>笔记</a><a>回顾</a>
        </nav>
        <div className={styles.fieldRailBottom}><span>W33</span><span>08:42</span></div>
      </aside>

      <section className={styles.fieldBody}>
        <header className={styles.fieldHeader}>
          <div>
            <p>2026 / 08 / 15 · FRIDAY</p>
            <h1>继续把问题<br/>变成证据。</h1>
          </div>
          <div className={styles.fieldWeather}>
            <span>本周脉搏</span><strong>18</strong><small>条有效推进</small>
          </div>
        </header>

        <div className={styles.fieldFocus}>
          <span className={styles.fieldIndex}>01</span>
          <div>
            <p className={styles.fieldKicker}>CURRENT THREAD / MINDFORGE</p>
            <h2>Session 与 CSRF 验收边界</h2>
            <p>把认证后的写请求固定成一条可复用、可解释的测试路径。</p>
          </div>
          <button type="button" onClick={() => setDone(!done)} className={done ? styles.fieldDone : ""}>
            {done ? "已完成" : "继续工作"}<Icon name="arrow" />
          </button>
        </div>

        <div className={styles.fieldTimeline}>
          <div className={styles.fieldDay}><span>今天</span><i /></div>
          <article>
            <time>08:12</time>
            <div><span className={styles.fieldType}>NOTE</span><h3>Controller 登录后的 CSRF token 生命周期</h3><p>匿名 token 会在认证后失效；下一步需要从认证 session 重新获取。</p></div>
            <span className={styles.fieldProject}>MindForge</span>
          </article>
          <article>
            <time>09:30</time>
            <div><span className={styles.fieldType}>TASK</span><h3>补全 Note 写接口的集成测试</h3><p>用真实登录与 CSRF 获取流程覆盖 201、401、403。</p></div>
            <span className={styles.fieldStatus}>进行中</span>
          </article>
          <div className={styles.fieldDay}><span>昨天</span><i /></div>
          <article className={styles.fieldQuiet}>
            <time>22:06</time>
            <div><span className={styles.fieldType}>LINK</span><h3>Spring Security Session Management</h3><p>docs.spring.io · 已归档到认证专题</p></div>
            <span className={styles.fieldProject}>Security</span>
          </article>
        </div>
      </section>

      <aside className={styles.fieldMargin}>
        <button type="button" aria-label="搜索"><Icon name="search" /></button>
        <div className={styles.fieldMarginNote}><span>边注 03</span><p>这周真正的进展不是完成了多少接口，而是认证测试开始形成统一语言。</p></div>
        <button type="button" className={styles.fieldCapture}><Icon name="plus" />快速记下</button>
      </aside>
    </main>
  );
}

function KnowledgeAtlas() {
  const [activeNode, setActiveNode] = useState("MindForge");
  const nodeDetails: Record<string, { eyebrow: string; title: string; body: string; meta: string[] }> = {
    MindForge: { eyebrow: "ACTIVE PROJECT", title: "MindForge", body: "把知识、项目推进与学习复盘收束到同一个个人工作台。", meta: ["Spring Boot", "Next.js", "PostgreSQL"] },
    Auth: { eyebrow: "KNOWLEDGE CLUSTER", title: "Session Security", body: "登录、Session fixation、CSRF 生命周期与安全边界。", meta: ["12 notes", "4 tasks", "2 decisions"] },
    Notes: { eyebrow: "NEXT MILESTONE", title: "Notes & Tags", body: "把认证基建连接到第一个真实业务资源。", meta: ["Milestone 2", "API first", "Markdown"] },
    Review: { eyebrow: "WEEKLY SIGNAL", title: "Week 33 Review", body: "认证链路已经闭合，下一阶段要避免测试工具函数掩盖协议断言。", meta: ["18 events", "6 completed", "1 blocker"] },
  };
  const detail = nodeDetails[activeNode];
  return (
    <main className={`${styles.surface} ${styles.atlas}`}>
      <header className={styles.atlasTopbar}>
        <div className={styles.atlasLogo}><span>mind</span><strong>forge</strong></div>
        <button type="button" className={styles.atlasSearch}><Icon name="search" /><span>搜索你的工作记忆</span><kbd>⌘ K</kbd></button>
        <div className={styles.atlasActions}><span>SYNCED · 08:42</span><button type="button"><Icon name="plus" /></button></div>
      </header>
      <aside className={styles.atlasSidebar}>
        <p>WORKSPACES</p>
        <button type="button" className={styles.atlasSideActive}><i className={styles.dotViolet}/>MindForge<span>18</span></button>
        <button type="button"><i className={styles.dotGold}/>Frontend Lab<span>7</span></button>
        <button type="button"><i className={styles.dotBlue}/>Interview Map<span>12</span></button>
        <p>LIBRARY</p>
        <button type="button"><Icon name="note"/>Notes<span>126</span></button>
        <button type="button"><Icon name="link"/>Sources<span>48</span></button>
        <button type="button"><Icon name="task"/>Tasks<span>9</span></button>
        <div className={styles.atlasPulse}><span>WEEKLY PULSE</span><strong>72%</strong><div><i /></div><small>6 of 8 focus items</small></div>
      </aside>

      <section className={styles.atlasMap}>
        <div className={styles.atlasHeading}><p>CONTEXT MAP / THIS WEEK</p><h1>你的工作正在<br/>形成什么？</h1></div>
        <svg className={styles.atlasLines} viewBox="0 0 800 620" preserveAspectRatio="none" aria-hidden="true">
          <path d="M390 320 C280 270 240 190 170 170"/><path d="M390 320 C520 260 600 210 660 150"/><path d="M390 320 C510 370 590 430 670 480"/><path d="M390 320 C300 400 240 470 180 500"/><path d="M170 170 C300 120 520 110 660 150"/>
        </svg>
        <button type="button" onClick={() => setActiveNode("MindForge")} className={`${styles.atlasNode} ${styles.nodeCenter} ${activeNode === "MindForge" ? styles.nodeActive : ""}`}><span>18</span><strong>MindForge</strong><small>active project</small></button>
        <button type="button" onClick={() => setActiveNode("Auth")} className={`${styles.atlasNode} ${styles.nodeAuth} ${activeNode === "Auth" ? styles.nodeActive : ""}`}><span>12</span><strong>Session<br/>Security</strong><small>knowledge</small></button>
        <button type="button" onClick={() => setActiveNode("Notes")} className={`${styles.atlasNode} ${styles.nodeNotes} ${activeNode === "Notes" ? styles.nodeActive : ""}`}><span>04</span><strong>Notes<br/>& Tags</strong><small>next milestone</small></button>
        <button type="button" onClick={() => setActiveNode("Review")} className={`${styles.atlasNode} ${styles.nodeReview} ${activeNode === "Review" ? styles.nodeActive : ""}`}><span>W33</span><strong>Weekly<br/>Review</strong><small>reflection</small></button>
        <div className={`${styles.atlasNode} ${styles.nodeLoose}`}><span>07</span><strong>Loose<br/>Threads</strong><small>inbox</small></div>
      </section>

      <aside className={styles.atlasInspector}>
        <div className={styles.atlasInspectorHead}><span>{detail.eyebrow}</span><button type="button">•••</button></div>
        <h2>{detail.title}</h2><p>{detail.body}</p>
        <div className={styles.atlasTags}>{detail.meta.map((tag) => <span key={tag}>{tag}</span>)}</div>
        <div className={styles.atlasSection}><span>NEXT ACTION</span><h3>补全认证写请求验收</h3><p>Today · High priority</p></div>
        <div className={styles.atlasSection}><span>RECENT SIGNALS</span><ul><li><i/>新增测试笔记 <time>12m</time></li><li><i/>完成 A3 CSRF <time>1d</time></li><li><i/>保存官方文档 <time>2d</time></li></ul></div>
        <button type="button" className={styles.atlasOpen}>进入上下文 <Icon name="arrow" /></button>
      </aside>
    </main>
  );
}

function CommandLedger() {
  const [capture, setCapture] = useState("");
  const [captured, setCaptured] = useState<string[]>([]);
  const submit = () => {
    if (!capture.trim()) return;
    setCaptured([capture.trim(), ...captured]);
    setCapture("");
  };
  return (
    <main className={`${styles.surface} ${styles.ledger}`}>
      <header className={styles.ledgerHeader}>
        <div className={styles.ledgerBrand}><b>MF/</b><span>PERSONAL OPERATING INDEX</span></div>
        <div className={styles.ledgerClock}>FRI 15 AUG <strong>08:42:17</strong></div>
        <button type="button">ARTORIA <span>●</span></button>
      </header>
      <nav className={styles.ledgerNav} aria-label="主导航">
        <a className={styles.ledgerNavActive}>00 OVERVIEW</a><a>01 NOTES</a><a>02 PROJECTS</a><a>03 TASKS</a><a>04 SOURCES</a><a>05 REVIEWS</a>
      </nav>
      <section className={styles.ledgerCommand}>
        <span>›</span><input value={capture} onChange={(event) => setCapture(event.target.value)} onKeyDown={(event) => event.key === "Enter" && submit()} placeholder="记录想法、粘贴链接，或输入 / 执行命令" aria-label="快速记录"/><kbd>ENTER</kbd>
      </section>
      <section className={styles.ledgerGrid}>
        <div className={styles.ledgerIntro}>
          <p>SYS.DASHBOARD / WEEK_33</p><h1>早上好。<br/>有 <mark>03</mark> 条线索<br/>值得继续。</h1>
          <div className={styles.ledgerStats}><div><span>COMPLETED</span><strong>06</strong><small>+2 vs W32</small></div><div><span>KNOWLEDGE</span><strong>18</strong><small>new signals</small></div><div><span>FOCUS</span><strong>72%</strong><small>weekly load</small></div></div>
        </div>
        <div className={styles.ledgerFocus}>
          <div className={styles.ledgerTitle}><span>ACTIVE QUEUE</span><button type="button">VIEW ALL ↗</button></div>
          <ol>
            <li><span>01</span><div><b>补全 Note 写接口集成测试</b><small>MINDFORGE / AUTH / HIGH</small></div><time>TODAY</time></li>
            <li><span>02</span><div><b>确定 Note API 的错误响应</b><small>MINDFORGE / API DESIGN</small></div><time>AUG 16</time></li>
            <li><span>03</span><div><b>整理 Session fixation 学习笔记</b><small>KNOWLEDGE / SECURITY</small></div><time>AUG 18</time></li>
          </ol>
        </div>
        <div className={styles.ledgerProjects}>
          <div className={styles.ledgerTitle}><span>PROJECT REGISTER</span><b>03 ACTIVE</b></div>
          <div className={styles.projectRow}><span>MF-01</span><b>MindForge</b><div><i style={{width:"72%"}}/></div><em>72%</em></div>
          <div className={styles.projectRow}><span>FL-02</span><b>Frontend Lab</b><div><i style={{width:"46%"}}/></div><em>46%</em></div>
          <div className={styles.projectRow}><span>IQ-03</span><b>Interview Map</b><div><i style={{width:"31%"}}/></div><em>31%</em></div>
        </div>
        <div className={styles.ledgerFeed}>
          <div className={styles.ledgerTitle}><span>INCOMING SIGNALS</span><b>{captured.length + 4} ITEMS</b></div>
          {captured.map((item, index) => <div className={styles.signalRow} key={`${item}-${index}`}><time>NOW</time><span className={styles.signalNew}>CAPTURE</span><p>{item}</p></div>)}
          <div className={styles.signalRow}><time>08:12</time><span>NOTE</span><p>认证后的 CSRF token 生命周期</p></div>
          <div className={styles.signalRow}><time>YDAY</time><span>LINK</span><p>Spring Security Session Management</p></div>
          <div className={styles.signalRow}><time>AUG 13</time><span>DONE</span><p>A3 · 真实 CSRF 测试生命周期</p></div>
        </div>
      </section>
    </main>
  );
}

function PrototypeSwitcher({ current, onChange }: { current: PrototypeVariant; onChange: (variant: PrototypeVariant) => void }) {
  const index = variants.indexOf(current);
  const move = useCallback((delta: number) => onChange(variants[(index + delta + variants.length) % variants.length]), [index, onChange]);
  useEffect(() => {
    const handleKey = (event: KeyboardEvent) => {
      const target = event.target as HTMLElement | null;
      if (target?.matches("input, textarea, [contenteditable='true']")) return;
      if (event.key === "ArrowLeft") move(-1);
      if (event.key === "ArrowRight") move(1);
    };
    window.addEventListener("keydown", handleKey);
    return () => window.removeEventListener("keydown", handleKey);
  }, [move]);
  return (
    <div className={styles.switcher}>
      <button type="button" onClick={() => move(-1)} aria-label="上一套方案">←</button>
      <div><strong>{current} — {variantMeta[current].name}</strong><span>{variantMeta[current].question}</span></div>
      <button type="button" onClick={() => move(1)} aria-label="下一套方案">→</button>
    </div>
  );
}

export default function DashboardPrototype({ initialVariant }: { initialVariant: PrototypeVariant }) {
  const router = useRouter();
  const pathname = usePathname();
  const changeVariant = useCallback((variant: PrototypeVariant) => {
    router.replace(`${pathname}?variant=${variant}`, { scroll: false });
  }, [pathname, router]);
  return (
    <>
      {initialVariant === "A" && <FieldLog />}
      {initialVariant === "B" && <KnowledgeAtlas />}
      {initialVariant === "C" && <CommandLedger />}
      {process.env.NODE_ENV !== "production" && <PrototypeSwitcher current={initialVariant} onChange={changeVariant} />}
    </>
  );
}
