// ─── Generic "Read" Reader Engine (Universal JSON Book Loader) ───────────────

var ReaderEngine = {
  _books: {},
  _activeBookId: 'bhagavad_gita',
  _currentIndex: 0,
  _activeBookData: null,
  _reflections: {},

  init: function(){
    // 1. Register default Bhagavad Gita
    this._books['bhagavad_gita'] = {
      id: 'bhagavad_gita',
      title: 'Bhagavad Gita',
      author: 'Vyasa',
      totalItems: 701,
      isDefault: true,
      source: 'gita.json'
    };

    // 2. Register bundled classic: Meditations by Marcus Aurelius (12 Books)
    this._books['meditations'] = {
      id: 'meditations',
      title: 'Meditations',
      author: 'Marcus Aurelius',
      totalItems: 12,
      isBundled: true,
      items: [
        {
          id: '1.1',
          chapter: 1,
          chapterTitle: 'Book 1 · Debts & Lessons',
          verse: 1,
          sectionLabel: 'BOOK 1 · SECTION 1',
          primaryText: 'From my grandfather Verus I learned good morals and the government of my temper.',
          secondaryText: 'Character and self-control are not inherited by chance, but cultivated through conscious example and daily practice.',
          commentary: 'Focus on cultivating patience, humility, and steady resolve regardless of external turmoil.'
        },
        {
          id: '2.1',
          chapter: 2,
          chapterTitle: 'Book 2 · On the River Gran',
          verse: 1,
          sectionLabel: 'BOOK 2 · SECTION 1',
          primaryText: 'When you wake up in the morning, tell yourself: The people I deal with today will be meddling, ungrateful, arrogant, dishonest, jealous, and surly.',
          secondaryText: 'They are like this because they cannot distinguish good from evil. But I have seen the beauty of good, and the ugliness of evil.',
          commentary: 'No one can hurt you or drag you into what is shameful unless you surrender your own integrity. We were made to work together.'
        },
        {
          id: '3.1',
          chapter: 3,
          chapterTitle: 'Book 3 · In Carnuntum',
          verse: 1,
          sectionLabel: 'BOOK 3 · SECTION 1',
          primaryText: 'We ought to observe also that even the things which follow after the things which are produced according to nature contain something pleasing and attractive.',
          secondaryText: 'Do not waste the remainder of your life in thoughts about others, when you do not refer your thoughts to some object of common utility.',
          commentary: 'Direct your attention solely to what is worthy of a rational human being. Avoid idle speculation.'
        },
        {
          id: '4.3',
          chapter: 4,
          chapterTitle: 'Book 4 · Inner Sanctuary',
          verse: 3,
          sectionLabel: 'BOOK 4 · SECTION 3',
          primaryText: 'People look for retreats for themselves, in the country, by the coast, or in the hills. There is nowhere that a person can find a more peaceful and trouble-free retreat than in his own mind.',
          secondaryText: 'Retreat into your own inner sanctuary constantly, and renew yourself there.',
          commentary: 'Keep your principles brief and fundamental, so that a quick visit to them clears away all distress.'
        },
        {
          id: '4.7',
          chapter: 4,
          chapterTitle: 'Book 4 · Inner Sanctuary',
          verse: 7,
          sectionLabel: 'BOOK 4 · SECTION 7',
          primaryText: 'Discard your misperceptions; stop being jerked like a puppet. Delimit the present moment.',
          secondaryText: 'Reject your sense of injury and the injury itself disappears.',
          commentary: 'Nothing outside your own reasoned judgment has the power to harm your character.'
        },
        {
          id: '5.1',
          chapter: 5,
          chapterTitle: 'Book 5 · Morning Duty',
          verse: 1,
          sectionLabel: 'BOOK 5 · SECTION 1',
          primaryText: 'At dawn, when you have trouble getting out of bed, tell yourself: I have to go to work — as a human being.',
          secondaryText: 'What do I have to complain of, if I\'m going to do what I was born for — the things I was brought into the world to do? Or is this what I was created for? To huddle under the blankets and stay warm?',
          commentary: 'Nature has set boundaries on sleep just as it has on eating and drinking. Step into your role with purpose.'
        },
        {
          id: '6.6',
          chapter: 6,
          chapterTitle: 'Book 6 · The Universe & Fellowship',
          verse: 6,
          sectionLabel: 'BOOK 6 · SECTION 6',
          primaryText: 'The best way of avenging yourself is not to become like the wrongdoer.',
          secondaryText: 'Take pleasure in one thing and rest in it: in passing from one social act to another, with memory of God.',
          commentary: 'Retaliation only multiplies the defect. Respond with unshakeable justice and poise.'
        },
        {
          id: '7.54',
          chapter: 7,
          chapterTitle: 'Book 7 · Mindfulness & Present',
          verse: 54,
          sectionLabel: 'BOOK 7 · SECTION 54',
          primaryText: 'Everywhere, at each moment, you have the option: to accept this circumstance with reverence, to treat this person with justice, to examine this impression with care.',
          secondaryText: 'Control your desires, extinguish your impulses, and keep your ruling mind in its own control.',
          commentary: 'Stoic mindfulness applied directly to daily interactions and circumstances.'
        },
        {
          id: '8.36',
          chapter: 8,
          chapterTitle: 'Book 8 · The Ruling Center',
          verse: 36,
          sectionLabel: 'BOOK 8 · SECTION 36',
          primaryText: 'Do not disturb yourself by picturing your life as a whole; do not assemble in your mind the many and varied troubles which have occurred or will occur.',
          secondaryText: 'In every present difficulty ask yourself: What is there in this that is unbearable and beyond endurance? You will be ashamed to confess it.',
          commentary: 'Bring the horizon down to the present moment. Bear what is right in front of you.'
        },
        {
          id: '9.1',
          chapter: 9,
          chapterTitle: 'Book 9 · Justice & Truth',
          verse: 1,
          sectionLabel: 'BOOK 9 · SECTION 1',
          primaryText: 'He who commits injustice is impious. For the nature of the universe having made rational animals for the sake of one another to help one another, he who transgresses her will is guilty of impiety.',
          secondaryText: 'And he who lies is also guilty of impiety toward the same divinity.',
          commentary: 'Cooperation and honesty are cosmic imperatives, not mere social conventions.'
        },
        {
          id: '10.16',
          chapter: 10,
          chapterTitle: 'Book 10 · Simplicity of Character',
          verse: 16,
          sectionLabel: 'BOOK 10 · SECTION 16',
          primaryText: 'Waste no more time arguing what a good man should be. Be one.',
          secondaryText: 'No more deliberation. No more postponement. Act with virtue right now.',
          commentary: 'The ultimate antidote to overthinking. Virtue is an action, not a theory.'
        },
        {
          id: '11.18',
          chapter: 11,
          chapterTitle: 'Book 11 · The Disciplined Mind',
          verse: 18,
          sectionLabel: 'BOOK 11 · SECTION 18',
          primaryText: 'Consider how much more often you suffer from your anger and grief than from those very things for which you are angry and grieved.',
          secondaryText: 'Kindness is invincible, provided only that it be sincere and not a hypocritical grin.',
          commentary: 'Your emotional reaction causes tenfold more damage than the original offense.'
        },
        {
          id: '12.36',
          chapter: 12,
          chapterTitle: 'Book 12 · The Final Perspective',
          verse: 36,
          sectionLabel: 'BOOK 12 · SECTION 36',
          primaryText: 'O man, you have been a citizen in this great city. What difference does it make to you whether for five years or three? For that which is conformable to the laws is just for all.',
          secondaryText: 'Depart then satisfied, for he also who releases you is satisfied.',
          commentary: 'Approach each day and your eventual departure with peace, gratitude, and dignity.'
        }
      ]
    };

    // 3. Register bundled classic: The Enchiridion by Epictetus (Loose Skeleton reference)
    this._books['stoic_handbook'] = {
      id: 'stoic_handbook',
      title: 'The Enchiridion',
      author: 'Epictetus',
      totalItems: 5,
      isBundled: true,
      items: [
        {
          id: '1',
          chapter: 'Chapter 1',
          title: 'Things in Our Control',
          content: 'Some things are in our control and others not. Things in our control are opinion, pursuit, desire, aversion, and, in a word, whatever are our own actions. Things not in our control are body, property, reputation, command, and, one word, whatever are not our own actions.',
          translation: 'Focus all your energy on what you can influence — your thoughts, actions, and desires. Do not attach your peace of mind to external outcomes.',
          notes: 'The fundamental dichotomy of control. Freedom begins with understanding what belongs to you and what belongs to the world.',
          tags: ['stoicism', 'control', 'peace']
        },
        {
          id: '2',
          chapter: 'Chapter 2',
          title: 'Managing Desire',
          content: 'Remember that desire demands the attainment of that of which you are desirous; and aversion demands the avoiding of that to which you are averse; that he who fails of the object of his desire is disappointed, and he who incurs the object of his aversion is wretched.',
          translation: 'If you desire only what is dependent on circumstances, you will inevitably be frustrated. Direct your aversion only to errors of your own moral character.',
          notes: 'Desire makes you fragile; mastering desire gives you tranquility.',
          tags: ['desire', 'clarity']
        },
        {
          id: '3',
          chapter: 'Chapter 3',
          title: 'The Nature of Things',
          content: 'With regard to whatever objects either delight the mind, or contribute to use, or are tenderly beloved, remind yourself of what nature they are, beginning with the merest trifles.',
          translation: 'Recognize the impermanence of objects and people you love, so that when change happens, you are not crushed.',
          notes: 'Memento mori and love without possessiveness.',
          tags: ['nature', 'impermanence']
        },
        {
          id: '4',
          chapter: 'Chapter 4',
          title: 'Entering Any Action',
          content: 'When you are about to take in hand any action, remind yourself what manner of thing it is. If you go to bathe, represent to yourself the things which usually happen in the bath: some people dash water about, others push, others use abusive language.',
          translation: 'Before starting any task, anticipate interruptions, rudeness, and delays so your poise is never broken.',
          notes: 'Premeditatio malorum: mental preparation prevents reactive frustration.',
          tags: ['action', 'preparation']
        },
        {
          id: '5',
          chapter: 'Chapter 5',
          title: 'Judgment of Events',
          content: 'Men are disturbed, not by things, but by the principles and notions which they form concerning things. Death, for instance, is not terrible, else it would have appeared so to Socrates.',
          translation: 'It is not what happens to you, but your judgment of what happens, that causes distress.',
          notes: 'Cognitive reframing: you cannot control the event, only your narrative of it.',
          tags: ['perception', 'judgment']
        }
      ]
    };

    // 4. Load custom imported books from localStorage
    try {
      var savedBooks = localStorage.getItem('v07_imported_books');
      if(savedBooks){
        var parsed = JSON.parse(savedBooks);
        for(var k in parsed){
          if(parsed.hasOwnProperty(k)){
            this._books[k] = parsed[k];
          }
        }
      }
      var activeId = localStorage.getItem('v07_active_book');
      if(activeId && this._books[activeId]){
        this._activeBookId = activeId;
      }
    } catch(e){}

    // Update UI
    this.loadBook(this._activeBookId);
  },

  getActiveBookInfo: function(){
    return this._books[this._activeBookId] || { title: 'Bhagavad Gita' };
  },

  getAllBooks: function(){
    var list = [];
    for(var k in this._books){
      if(this._books.hasOwnProperty(k)){
        list.push(this._books[k]);
      }
    }
    return list;
  },

  removeBook: function(bookId){
    var book = this._books[bookId];
    if(!book) return false;
    if(book.isDefault || book.isBundled) return false;
    delete this._books[bookId];
    var customBooks = {};
    for(var k in this._books){
      if(this._books[k] && !this._books[k].isDefault && !this._books[k].isBundled){
        customBooks[k] = this._books[k];
      }
    }
    try { localStorage.setItem('v07_imported_books', JSON.stringify(customBooks)); } catch(e){}
    try { localStorage.removeItem('v07_book_read_' + bookId); } catch(e){}
    try { localStorage.removeItem('v07_book_pos_' + bookId); } catch(e){}
    if(this._activeBookId === bookId){
      this.loadBook('bhagavad_gita');
    }
    return true;
  },

  loadBook: function(bookId){
    var book = this._books[bookId];
    if(!book) return;
    this._activeBookId = bookId;
    try { localStorage.setItem('v07_active_book', bookId); } catch(e){}

    var titleEl = document.getElementById('activeBookName');
    if(titleEl) titleEl.textContent = book.title.toUpperCase();

    if(bookId === 'bhagavad_gita'){
      // Delegate to existing Gita API
      if(typeof loadDashboard === 'function'){
        fetch(BASE + '/gita/today').then(function(r){ return r.json(); }).then(function(v){
          renderGita(v);
        }).catch(function(){});
      }
    } else {
      // Use local book items
      this._currentIndex = 0;
      this.renderCurrentItem();
    }

    // If the sidebar is currently open, refresh it
    var sidebar = document.getElementById('gitaSidebar');
    if(sidebar && sidebar.classList.contains('open')){
      this.renderSidebar();
    }

    if(typeof updateDashboardData === 'function') updateDashboardData({});
  },

  importBookJson: function(rawContent, filename){
    try {
      var parsed = null;
      var isJson = false;
      if(typeof rawContent === 'object' && rawContent !== null){
        parsed = rawContent;
        isJson = true;
      } else if(typeof rawContent === 'string'){
        var trimmed = rawContent.trim();
        if(trimmed.startsWith('{') || trimmed.startsWith('[')){
          try {
            parsed = JSON.parse(trimmed);
            isJson = true;
          } catch(e){}
        }
      }

      var id = 'book_' + Date.now();
      var defaultTitle = (filename || 'Imported Book').replace(/\.[^/.]+$/, '').replace(/[_\-]/g, ' ');
      var title = defaultTitle;
      var author = 'Unknown';
      var desc = '';
      var rawItems = [];

      if(isJson && parsed){
        id = parsed.id || id;
        title = parsed.title || parsed.name || defaultTitle;
        author = parsed.author || parsed.writer || author;
        desc = parsed.description || parsed.about || '';

        if(Array.isArray(parsed)){
          rawItems = parsed;
        } else if(Array.isArray(parsed.items)){
          rawItems = parsed.items;
        } else if(Array.isArray(parsed.sections)){
          rawItems = parsed.sections;
        } else if(Array.isArray(parsed.verses)){
          rawItems = parsed.verses;
        } else if(Array.isArray(parsed.data)){
          rawItems = parsed.data;
        } else if(Array.isArray(parsed.chapters)){
          parsed.chapters.forEach(function(ch, chIdx){
            var chTitle = ch.title || ch.name || ('Chapter ' + (ch.chapter || (chIdx + 1)));
            var subItems = ch.items || ch.verses || ch.sections || [ch];
            subItems.forEach(function(sub){
              if(typeof sub === 'string'){
                rawItems.push({ chapter: chTitle, text: sub });
              } else if(typeof sub === 'object' && sub !== null){
                var itemObj = Object.assign({}, sub);
                if(!itemObj.chapter) itemObj.chapter = chTitle;
                rawItems.push(itemObj);
              }
            });
          });
        } else if(typeof parsed === 'object'){
          var keys = Object.keys(parsed).filter(function(k){ return k !== 'id' && k !== 'title' && k !== 'author' && k !== 'description'; });
          if(keys.length && typeof parsed[keys[0]] === 'object'){
            rawItems = keys.map(function(k){ return parsed[k]; });
          }
        }
      } else {
        var text = String(rawContent || '');
        var paragraphs = text.split(/\n\s*\n/).map(function(p){ return p.trim(); }).filter(function(p){ return p.length > 0; });
        rawItems = paragraphs.map(function(p, idx){
          return {
            id: String(idx + 1),
            chapter: 'Passage ' + (idx + 1),
            content: p
          };
        });
      }

      if(!rawItems.length){
        toast('No readable text or sections found in file');
        return false;
      }

      var formattedBook = {
        id: id,
        title: title,
        author: author,
        description: desc,
        totalItems: rawItems.length,
        items: rawItems.map(function(it, idx){
          if(typeof it === 'string'){
            return {
              id: String(idx + 1),
              chapter: 'Passage ' + (idx + 1),
              chapterTitle: 'Passage ' + (idx + 1),
              title: 'Passage ' + (idx + 1),
              sectionLabel: 'SECTION ' + (idx + 1),
              verse: idx + 1,
              content: it,
              primaryText: it,
              translation: '',
              secondaryText: '',
              notes: '',
              commentary: ''
            };
          }
          var primary = it.content || it.primaryText || it.text || it.shloka?.text || it.shloka || it.passage || it.quote || it.body || '';
          var secondary = it.translation || it.secondaryText || it.english_sivananda?.text || it.english_sivananda || it.english || it.meaning || '';
          var notes = it.notes || it.commentary || it.hindi_commentary?.text || it.hindi_commentary || it.reflection || it.principle || '';
          var ch = it.chapter || it.chapterTitle || (it.book ? ('Book ' + it.book) : (it.chapter ? ('Chapter ' + it.chapter) : 'General'));
          var sec = it.title || it.sectionLabel || (it.verse ? ('Section ' + it.verse) : ('Section ' + (idx + 1)));

          return {
            id: String(it.id || (idx + 1)),
            chapter: String(ch),
            chapterTitle: String(ch),
            title: String(sec),
            sectionLabel: String(sec),
            verse: it.verse || (idx + 1),
            content: primary,
            primaryText: primary,
            translation: secondary,
            secondaryText: secondary,
            notes: notes,
            commentary: notes,
            tags: it.tags || []
          };
        })
      };

      this._books[id] = formattedBook;

      // Persist custom book
      var customBooks = {};
      for(var k in this._books){
        if(this._books[k] && !this._books[k].isDefault && !this._books[k].isBundled){
          customBooks[k] = this._books[k];
        }
      }
      try { localStorage.setItem('v07_imported_books', JSON.stringify(customBooks)); } catch(e){}

      toast('Book "' + title + '" loaded successfully!');
      this.loadBook(id);
      return true;
    } catch(err){
      toast('Failed to load book: ' + err.message);
      return false;
    }
  },

  getCurrentItem: function(){
    if(this._activeBookId === 'bhagavad_gita'){
      return window.gitaData || null;
    }
    var book = this._books[this._activeBookId];
    if(book && book.items && book.items[this._currentIndex]){
      return book.items[this._currentIndex];
    }
    return null;
  },

  // ─── Read Status Tracking (per book) ──────────────────────────────────────
  getReadList: function(bookId){
    try {
      return JSON.parse(localStorage.getItem('v07_book_read_' + (bookId || this._activeBookId)) || '[]');
    } catch(e){ return []; }
  },

  isRead: function(bookId, itemId){
    var list = this.getReadList(bookId);
    return list.indexOf(String(itemId)) !== -1;
  },

  setRead: function(bookId, itemId, status){
    var list = this.getReadList(bookId);
    var strId = String(itemId);
    var idx = list.indexOf(strId);
    if(status && idx === -1){
      list.push(strId);
    } else if(!status && idx !== -1){
      list.splice(idx, 1);
    }
    try {
      localStorage.setItem('v07_book_read_' + bookId, JSON.stringify(list));
    } catch(e){}
  },

  renderCurrentItem: function(){
    var item = this.getCurrentItem();
    if(!item) return;

    var chLabel = document.getElementById('gitaChapterLabel');
    var vCounter = document.getElementById('gitaVerseCounter');
    var shloka = document.getElementById('gitaPageShloka');
    var trans = document.getElementById('gitaPageTrans');
    var transSection = document.getElementById('gitaTransSection');
    var htSection = document.getElementById('gitaHindiTransSection');
    var commSection = document.getElementById('gitaHindiCommSection');
    var commText = document.getElementById('gitaPageHindiComm');
    var gbSection = document.getElementById('gitaGambSection');

    // Extract loose skeleton fields
    var primary = item.content || item.primaryText || item.text || item.shloka?.text || item.shloka || '';
    var secondary = item.translation || item.secondaryText || item.english_sivananda?.text || item.english_sivananda || '';
    var notes = item.notes || item.commentary || item.hindi_commentary?.text || item.hindi_commentary || item.reflection || item.principle || '';
    var ch = item.chapter || item.chapterTitle || '';
    var sec = item.title || item.sectionLabel || '';

    var book = this._books[this._activeBookId] || {};
    var total = (book.items && book.items.length) || 1;

    if(chLabel) chLabel.textContent = ch ? ch.toUpperCase() : (book.title ? book.title.toUpperCase() : 'PASSAGE');
    if(vCounter) vCounter.textContent = sec ? (sec.toUpperCase() + ' · ' + (this._currentIndex + 1) + ' / ' + total) : ((this._currentIndex + 1) + ' / ' + total);
    if(shloka) shloka.textContent = primary;

    // Translation section
    if(secondary){
      if(transSection) transSection.style.display = 'block';
      if(trans) trans.textContent = secondary;
    } else {
      if(transSection) transSection.style.display = 'none';
    }

    // STRICTLY hide Gita-specific translation sections for other books
    if(htSection) htSection.style.display = 'none';
    if(gbSection) gbSection.style.display = 'none';

    // Notes / Commentary section
    if(notes){
      if(commSection){
        commSection.style.display = 'block';
        var commTitle = commSection.querySelector('div');
        if(commTitle) commTitle.textContent = 'NOTES & COMMENTARY';
      }
      if(commText) commText.textContent = notes;
    } else {
      if(commSection) commSection.style.display = 'none';
    }

    var actSec = document.getElementById('gitaActionsSection');
    if(actSec) actSec.style.display = 'flex';

    var readBtn = document.getElementById('gitaPageReadBtn');
    var isReadNow = this.isRead(this._activeBookId, item.id);
    if(readBtn){
      readBtn.textContent = isReadNow ? 'MARK UNREAD' : 'MARK READ';
      readBtn.style.color = isReadNow ? 'var(--yellow)' : 'var(--primary)';
      readBtn.style.borderColor = isReadNow ? 'var(--yellow)' : 'var(--primary)';
    }

    // Progress display
    var readList = this.getReadList(this._activeBookId);
    var readCount = readList.length;
    var progEl = document.getElementById('gitaPageProgress');
    if(progEl) progEl.textContent = readCount + '/' + total + ' read';

    var dotsEl = document.getElementById('gitaProgressDots');
    if(dotsEl){
      var stepCount = Math.min(Math.max(Math.min(total, 20), 5), 20);
      var currentStep = Math.floor((this._currentIndex / Math.max(total - 1, 1)) * (stepCount - 1));
      var dots = '';
      for(var d = 0; d < stepCount; d++){
        if(d === currentStep){
          dots += '<div style="width:2px;height:16px;background:var(--primary);border-radius:1px"></div>';
        } else {
          dots += '<div style="width:4px;height:4px;background:var(--border);border-radius:50%"></div>';
        }
      }
      dotsEl.innerHTML = '<div style="display:flex;flex-direction:column;gap:8px;align-items:center">' + dots + '</div>';
    }

    // Ensure all active reading sections are visible immediately
    var sections = document.querySelectorAll('#gitaReadingArea .gita-section');
    sections.forEach(function(s){
      if(s.style.display !== 'none'){
        s.classList.add('visible');
      }
    });

    // Also update thoughts if any
    this.renderThoughtsForItem(item.id);

    if(typeof updateDashboardData === 'function') updateDashboardData({});
  },

  next: function(){
    if(this._activeBookId === 'bhagavad_gita'){
      if(typeof navGita === 'function') navGita('next');
      return;
    }
    var book = this._books[this._activeBookId];
    if(book && book.items && this._currentIndex < book.items.length - 1){
      this._currentIndex++;
      this.renderCurrentItem();
      var area = document.getElementById('gitaReadingArea');
      if(area) area.scrollTop = 0;
    } else {
      toast('Reached the end of this book');
    }
  },

  prev: function(){
    if(this._activeBookId === 'bhagavad_gita'){
      if(typeof navGita === 'function') navGita('prev');
      return;
    }
    if(this._currentIndex > 0){
      this._currentIndex--;
      this.renderCurrentItem();
      var area = document.getElementById('gitaReadingArea');
      if(area) area.scrollTop = 0;
    }
  },

  toggleRead: function(){
    if(this._activeBookId === 'bhagavad_gita'){
      if(typeof toggleGitaRead === 'function') toggleGitaRead();
      return;
    }
    var item = this.getCurrentItem();
    if(!item) return;

    var cur = this.isRead(this._activeBookId, item.id);
    this.setRead(this._activeBookId, item.id, !cur);
    toast(!cur ? 'Marked read' : 'Marked unread');
    this.renderCurrentItem();

    // If sidebar is open, refresh tracker list
    var sidebar = document.getElementById('gitaSidebar');
    if(sidebar && sidebar.classList.contains('open')){
      this.renderSidebar();
    }
  },

  jumpToItem: function(idx){
    var book = this._books[this._activeBookId];
    if(!book || !book.items || !book.items[idx]) return;
    this._currentIndex = idx;
    this.renderCurrentItem();
    if(typeof closeGitaSidebar === 'function') closeGitaSidebar();
    var area = document.getElementById('gitaReadingArea');
    if(area) area.scrollTop = 0;
  },

  // ─── Reflection / Thoughts per Item ───────────────────────────────────────
  saveThought: function(text){
    var item = this.getCurrentItem();
    if(!item) return;
    var key = 'v07_book_thoughts_' + this._activeBookId;
    var allThoughts = {};
    try { allThoughts = JSON.parse(localStorage.getItem(key) || '{}'); } catch(e){}
    if(!allThoughts[item.id]) allThoughts[item.id] = [];
    allThoughts[item.id].unshift({ text: text, ts: new Date().toISOString() });
    try { localStorage.setItem(key, JSON.stringify(allThoughts)); } catch(e){}
    this.renderThoughtsForItem(item.id);
  },

  renderThoughtsForItem: function(itemId){
    var container = document.getElementById('gitaVerseThoughts');
    if(!container) return;
    if(this._activeBookId === 'bhagavad_gita') return; // Handled by loadGitaVerseThoughts()

    var key = 'v07_book_thoughts_' + this._activeBookId;
    var allThoughts = {};
    try { allThoughts = JSON.parse(localStorage.getItem(key) || '{}'); } catch(e){}
    var list = allThoughts[itemId] || [];
    if(!list.length){
      container.innerHTML = '';
      return;
    }
    var h = '<div style="font-family:\'Space Mono\',monospace;font-size:10px;color:var(--text-inactive);text-transform:uppercase;letter-spacing:0.1em;margin-bottom:8px">REFLECTIONS</div>';
    list.forEach(function(t){
      h += '<div style="background:var(--surface);border:1px solid var(--border);border-radius:6px;padding:10px;margin-bottom:6px;font-size:12px;color:var(--text);line-height:1.5">';
      h +=   esc(t.text);
      h += '</div>';
    });
    container.innerHTML = h;
  },

  // ─── Dynamic Reader Sidebar Linking ─────────────────────────────────────────
  renderSidebar: function(){
    var titleEl = document.getElementById('readerSidebarTitle');
    var progressEl = document.getElementById('gitaTrackerProgress');
    var listEl = document.getElementById('gitaTrackerList');
    if(!listEl) return;

    if(this._activeBookId === 'bhagavad_gita'){
      if(titleEl) titleEl.textContent = 'READING TRACKER';
      if(typeof loadGitaTracker === 'function') loadGitaTracker();
      return;
    }

    var book = this.getActiveBookInfo();
    var bookTitle = book.title || 'BOOK';
    if(titleEl){
      titleEl.textContent = bookTitle.toUpperCase() + ' TRACKER';
    }

    var items = book.items || [];
    var readList = this.getReadList(this._activeBookId);
    var readCount = items.filter(function(it){ return readList.indexOf(it.id) !== -1; }).length;
    var total = items.length;

    if(progressEl){
      progressEl.innerHTML =
        '<span style="color:var(--secondary)">' + readCount + '</span>' +
        '<span style="color:var(--text-inactive)">/' + total + ' read</span>' +
        '<span style="margin-left:8px;color:var(--text-inactive)">(' + (total - readCount) + ' remaining)</span>';
    }

    // Group items by chapter if chapters exist
    var chapters = {};
    var chapterOrder = [];
    items.forEach(function(it, idx){
      var chNum = it.chapter || 1;
      var chTitle = it.chapterTitle || ('Chapter ' + chNum);
      if(!chapters[chNum]){
        chapters[chNum] = { number: chNum, title: chTitle, items: [] };
        chapterOrder.push(chNum);
      }
      chapters[chNum].items.push({ item: it, index: idx });
    });

    var html = '';
    var self = this;

    if(chapterOrder.length > 1){
      // Accordion by Chapter
      chapterOrder.forEach(function(chNum){
        var ch = chapters[chNum];
        var chTotal = ch.items.length;
        var chRead = ch.items.filter(function(ci){ return readList.indexOf(ci.item.id) !== -1; }).length;
        var isCurrentCh = ch.items.some(function(ci){ return ci.index === self._currentIndex; });

        html += '<div style="margin-bottom:8px;border:1px solid var(--border);border-radius:8px;overflow:hidden;background:var(--surface)">';
        html += '  <div onclick="ReaderEngine.toggleSidebarChapter(' + chNum + ')" style="display:flex;justify-content:space-between;align-items:center;padding:10px 12px;cursor:pointer;user-select:none;background:var(--surface-container-low)">';
        html += '    <div style="min-width:0;flex:1">';
        html += '      <div style="font-family:\'Space Mono\',monospace;font-size:11px;font-weight:700;color:var(--text);white-space:nowrap;overflow:hidden;text-overflow:ellipsis">' + esc(ch.title) + '</div>';
        html += '      <div style="font-size:10px;color:var(--text-inactive);margin-top:2px">' + chRead + '/' + chTotal + ' read</div>';
        html += '    </div>';
        html += '    <span id="chReaderArrow_' + chNum + '" style="font-size:12px;color:var(--text-inactive);transition:transform 0.2s;' + (isCurrentCh ? 'transform:rotate(90deg)' : '') + '">▶</span>';
        html += '  </div>';

        html += '  <div id="chReaderItems_' + chNum + '" style="display:' + (isCurrentCh ? 'block' : 'none') + ';padding:4px 8px;border-top:1px solid var(--divider)">';
        ch.items.forEach(function(ci){
          var isItemRead = readList.indexOf(ci.item.id) !== -1;
          var isCurrent = ci.index === self._currentIndex;
          var activeStyle = isCurrent ? 'background:rgba(167,200,255,0.08);border-left:3px solid var(--primary);' : '';

          html += '<div onclick="ReaderEngine.jumpToItem(' + ci.index + ')" style="display:flex;align-items:center;justify-content:space-between;padding:8px 8px;border-bottom:1px solid var(--divider);cursor:pointer;' + activeStyle + '">';
          html += '  <div style="min-width:0;flex:1;padding-right:8px">';
          html += '    <div style="font-family:\'Space Mono\',monospace;font-size:10px;font-weight:600;color:' + (isCurrent ? 'var(--primary)' : 'var(--text)') + '">' + esc(ci.item.sectionLabel || ('SEC ' + ci.item.verse)) + '</div>';
          if(ci.item.primaryText){
            var snippet = ci.item.primaryText.substring(0, 45) + (ci.item.primaryText.length > 45 ? '...' : '');
            html += '    <div style="font-size:10px;color:var(--text-inactive);margin-top:2px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">' + esc(snippet) + '</div>';
          }
          html += '  </div>';
          html += '  <span style="font-size:13px;color:' + (isItemRead ? 'var(--secondary)' : 'var(--text-inactive)') + ';flex-shrink:0">' + (isItemRead ? '✓' : '○') + '</span>';
          html += '</div>';
        });
        html += '  </div>';
        html += '</div>';
      });
    } else {
      // Flat list of sections
      items.forEach(function(it, idx){
        var isItemRead = readList.indexOf(it.id) !== -1;
        var isCurrent = idx === self._currentIndex;
        var activeStyle = isCurrent ? 'background:rgba(167,200,255,0.08);border-left:3px solid var(--primary);' : '';

        html += '<div onclick="ReaderEngine.jumpToItem(' + idx + ')" style="display:flex;align-items:center;justify-content:space-between;padding:10px 10px;border-bottom:1px solid var(--divider);cursor:pointer;' + activeStyle + '">';
        html += '  <div style="min-width:0;flex:1;padding-right:8px">';
        html += '    <div style="font-family:\'Space Mono\',monospace;font-size:11px;font-weight:600;color:' + (isCurrent ? 'var(--primary)' : 'var(--text)') + '">' + esc(it.sectionLabel || ('SECTION ' + (idx + 1))) + '</div>';
        if(it.primaryText){
          var snippet = it.primaryText.substring(0, 50) + (it.primaryText.length > 50 ? '...' : '');
          html += '    <div style="font-size:10px;color:var(--text-inactive);margin-top:2px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">' + esc(snippet) + '</div>';
        }
        html += '  </div>';
        html += '  <span style="font-size:14px;color:' + (isItemRead ? 'var(--secondary)' : 'var(--text-inactive)') + ';flex-shrink:0">' + (isItemRead ? '✓' : '○') + '</span>';
        html += '</div>';
      });
    }

    listEl.innerHTML = html;
  },

  toggleSidebarChapter: function(chNum){
    var el = document.getElementById('chReaderItems_' + chNum);
    var arrow = document.getElementById('chReaderArrow_' + chNum);
    if(!el) return;
    if(el.style.display === 'none' || !el.style.display){
      el.style.display = 'block';
      if(arrow) arrow.style.transform = 'rotate(90deg)';
    } else {
      el.style.display = 'none';
      if(arrow) arrow.style.transform = 'rotate(0deg)';
    }
  }
};

// ─── Book Library Modal & Management ──────────────────────────────────────────
function openBookLibraryModal(){
  var modal = document.getElementById('bookLibraryModal');
  var list = document.getElementById('bookLibraryList');
  if(!modal || !list) return;

  var books = ReaderEngine.getAllBooks();
  var html = '';

  books.forEach(function(b){
    var isActive = b.id === ReaderEngine._activeBookId;
    var isCustom = !b.isDefault && !b.isBundled;
    var totalCount = (b.totalItems || (b.items ? b.items.length : 0)) || 1;
    var readCount = ReaderEngine.getReadList(b.id).length;
    var pct = Math.min(Math.round((readCount / totalCount) * 100), 100);

    html += '<div class="book-list-item ' + (isActive ? 'active' : '') + '" style="display:flex;flex-direction:column;gap:8px;padding:12px;border:1px solid ' + (isActive ? 'var(--primary)' : 'var(--border)') + ';border-radius:8px;background:' + (isActive ? 'rgba(167,200,255,0.06)' : 'var(--surface)') + ';margin-bottom:8px;transition:all 0.15s">';
    html += '  <div style="display:flex;align-items:center;justify-content:space-between;cursor:pointer" onclick="selectBook(\'' + b.id + '\')">';
    html += '    <div style="display:flex;align-items:center;gap:10px;min-width:0;flex:1">';
    html += '      <span style="font-size:22px;flex-shrink:0">' + (isCustom ? '📄' : '📖') + '</span>';
    html += '      <div style="min-width:0;flex:1">';
    html += '        <div style="font-weight:600;font-size:13px;color:var(--text);white-space:nowrap;overflow:hidden;text-overflow:ellipsis">' + esc(b.title) + '</div>';
    html += '        <div style="font-size:10px;color:var(--text-inactive);margin-top:2px">' + esc(b.author || (isCustom ? 'Custom File' : 'Classic')) + ' · ' + totalCount + ' sections</div>';
    html += '      </div>';
    html += '    </div>';
    if(isActive){
      html += '    <span style="color:var(--secondary);font-family:\'Space Mono\',monospace;font-size:10px;font-weight:700;flex-shrink:0;margin-left:8px">ACTIVE ✓</span>';
    } else {
      html += '    <button class="btn btn-outline" style="font-size:10px;padding:4px 8px;flex-shrink:0;margin-left:8px" onclick="event.stopPropagation();selectBook(\'' + b.id + '\')">READ</button>';
    }
    html += '  </div>';

    // Reading progress bar
    html += '  <div style="display:flex;align-items:center;gap:8px;padding-top:4px;border-top:1px solid rgba(255,255,255,0.04)">';
    html += '    <div style="flex:1;height:4px;background:var(--surface-container-low);border-radius:2px;overflow:hidden">';
    html += '      <div style="height:100%;width:' + pct + '%;background:' + (pct >= 100 ? 'var(--secondary)' : 'var(--primary)') + ';border-radius:2px;transition:width 0.2s"></div>';
    html += '    </div>';
    html += '    <span style="font-family:\'JetBrains Mono\',monospace;font-size:9px;color:var(--text-inactive);flex-shrink:0">' + readCount + '/' + totalCount + ' (' + pct + '%)</span>';
    html += '    <div style="display:flex;gap:4px;margin-left:auto">';
    if(readCount > 0){
      html += '      <button onclick="event.stopPropagation();resetBookProgress(\'' + b.id + '\')" title="Reset progress" style="background:transparent;border:none;color:var(--text-inactive);font-size:11px;cursor:pointer;padding:2px 4px">↺</button>';
    }
    if(isCustom){
      html += '      <button onclick="event.stopPropagation();deleteCustomBook(\'' + b.id + '\')" title="Delete custom book" style="background:transparent;border:none;color:var(--red);font-size:12px;cursor:pointer;padding:2px 4px">✕</button>';
    }
    html += '    </div>';
    html += '  </div>';
    html += '</div>';
  });

  list.innerHTML = html;
  modal.classList.add('show');
}

function closeBookLibraryModal(){
  var modal = document.getElementById('bookLibraryModal');
  if(modal) modal.classList.remove('show');
}

function selectBook(bookId){
  ReaderEngine.loadBook(bookId);
  closeBookLibraryModal();
  populateReaderSettings();
  toast('Active book: ' + ReaderEngine.getActiveBookInfo().title);
}

function deleteCustomBook(bookId){
  var book = ReaderEngine._books[bookId];
  var title = book ? book.title : 'this book';
  if(!confirm('Delete "' + title + '" from your library?')) return;
  if(ReaderEngine.removeBook(bookId)){
    openBookLibraryModal();
    populateReaderSettings();
    toast('Book removed');
  } else {
    toast('Cannot remove bundled books');
  }
}

function resetBookProgress(bookId){
  if(!confirm('Reset read progress for this book?')) return;
  try { localStorage.removeItem('v07_book_read_' + bookId); } catch(e){}
  try { localStorage.removeItem('v07_book_pos_' + bookId); } catch(e){}
  openBookLibraryModal();
  if(ReaderEngine._activeBookId === bookId){
    ReaderEngine.jumpToItem(0);
  }
  toast('Progress reset');
}

function setReaderFontSize(size){
  try { localStorage.setItem('v07_reader_font_size', size); } catch(e){}
  applyReaderPrefs();
}

function toggleReaderPref(key, val){
  try { localStorage.setItem('v07_reader_' + key, val ? '1' : '0'); } catch(e){}
  applyReaderPrefs();
}

function applyReaderPrefs(){
  var size = localStorage.getItem('v07_reader_font_size') || 'medium';
  var pxMap = { small: '18px', medium: '22px', large: '26px', xlarge: '30px' };
  var shloka = document.getElementById('gitaPageShloka');
  if(shloka) shloka.style.fontSize = pxMap[size] || '22px';

  var showOrig = localStorage.getItem('v07_reader_show_original') !== '0';
  var origSec = document.getElementById('gitaShlokaSection');
  if(origSec) origSec.style.display = showOrig ? 'block' : 'none';

  var showTrans = localStorage.getItem('v07_reader_show_trans') !== '0';
  var transSec = document.getElementById('gitaTransSection');
  if(transSec){
    var curItem = ReaderEngine.getCurrentItem();
    var hasTrans = curItem && (curItem.translation || curItem.secondaryText || curItem.english_sivananda);
    transSec.style.display = (showTrans && hasTrans) ? 'block' : 'none';
  }
}

function populateReaderSettings(){
  var select = document.getElementById('readerActiveBookSelect');
  if(select){
    var books = ReaderEngine.getAllBooks();
    var cur = ReaderEngine._activeBookId;
    select.innerHTML = books.map(function(b){
      return '<option value="' + b.id + '"' + (b.id === cur ? ' selected' : '') + '>' + esc(b.title) + '</option>';
    }).join('');
  }
  var fontSel = document.getElementById('readerFontSizeSelect');
  if(fontSel){
    fontSel.value = localStorage.getItem('v07_reader_font_size') || 'medium';
  }
  var origToggle = document.getElementById('readerShowOriginalToggle');
  if(origToggle){
    origToggle.checked = localStorage.getItem('v07_reader_show_original') !== '0';
  }
  var transToggle = document.getElementById('readerShowTransToggle');
  if(transToggle){
    transToggle.checked = localStorage.getItem('v07_reader_show_trans') !== '0';
  }
  applyReaderPrefs();
}

function triggerImportBook(){
  var input = document.getElementById('bookJsonFileInput');
  if(input) input.click();
}

function handleBookFileSelected(event){
  var file = event.target.files && event.target.files[0];
  if(!file) return;

  var reader = new FileReader();
  reader.onload = function(e){
    var content = e.target.result;
    if(ReaderEngine.importBookJson(content, file.name)){
      closeBookLibraryModal();
      populateReaderSettings();
    }
    if(event.target) event.target.value = '';
  };
  reader.readAsText(file);
}

// Initialize ReaderEngine
if(document.readyState === 'loading'){
  document.addEventListener('DOMContentLoaded', function(){
    ReaderEngine.init();
    applyReaderPrefs();
  });
} else {
  ReaderEngine.init();
  applyReaderPrefs();
}
