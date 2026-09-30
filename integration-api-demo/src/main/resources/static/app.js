'use strict';
const $ = (id) => document.getElementById(id);
const state = { view: 'search', keyword: '', searchPage: 1, favoritePage: 0, end: true, request: 0 };
const saved = new Set();
function el(tag, text, className) { const node = document.createElement(tag); if (text !== undefined) node.textContent = text; if (className) node.className = className; return node; }
function safeUrl(value) { try { const url = new URL(value); return ['https:', 'http:'].includes(url.protocol) ? url.href : null; } catch { return null; } }
async function api(path, options = {}) {
  const response = await fetch(path, { ...options, headers: { 'Content-Type': 'application/json', ...options.headers } });
  if (response.status === 204) return null;
  const body = await response.json().catch(() => null);
  if (!response.ok) { const error = new Error(body?.message || `요청을 처리하지 못했습니다. (${response.status})`); error.code = body?.code; throw error; }
  return body;
}
function feedback(message, error = false) { $('feedback').textContent = message; $('feedback').className = error ? 'error' : ''; $('feedback').hidden = !message; }
function empty(title, description) { $('empty').replaceChildren(el('h3', title), el('p', description)); $('empty').hidden = false; }
function cover(book, className) { const url = safeUrl(book.thumbnail); if (!url) return el('span', 'BOOK', 'cover-placeholder'); const img = el('img', undefined, className); img.src = url; img.alt = `${book.title} 표지`; img.loading = 'lazy'; img.referrerPolicy = 'no-referrer'; img.addEventListener('error', () => img.replaceWith(el('span', 'BOOK', 'cover-placeholder')), { once: true }); return img; }
async function saveBook(book, button) {
  button.disabled = true; button.textContent = '저장 중…';
  try { await api('/api/favorites', { method: 'POST', body: JSON.stringify({ isbn: book.isbn }) }); saved.add(book.isbn); button.textContent = '✓ 저장됨'; feedback('공용 서재에 저장했습니다.'); refreshCount(); }
  catch (error) { if (error.code === 'FAVORITE_ALREADY_EXISTS') { saved.add(book.isbn); button.textContent = '✓ 이미 저장된 책'; } else { button.disabled = false; button.textContent = '+ 서재에 담기'; } feedback(error.message, true); }
}
function saveButton(book) { const button = el('button', !book.isbn ? 'ISBN 정보 없음' : saved.has(book.isbn) ? '✓ 저장됨' : '+ 서재에 담기', 'save'); button.disabled = !book.isbn || saved.has(book.isbn); button.addEventListener('click', () => saveBook(book, button)); return button; }
let detailRequest = 0;
async function showDetail(book) {
  const request = ++detailRequest; $('detail-content').replaceChildren(el('p', '도서 정보를 불러오는 중…')); $('detail').showModal();
  try {
    const data = book.isbn ? await api(`/api/books/${encodeURIComponent(book.isbn)}`) : book;
    if (request !== detailRequest || !$('detail').open) return;
    const content = $('detail-content'); const actions = el('div', undefined, 'detail-actions'); actions.append(saveButton(data));
    const url = safeUrl(data.url); if (url) { const link = el('a', '도서 원문 보기 ↗'); link.href = url; link.target = '_blank'; link.rel = 'noopener noreferrer'; actions.append(link); }
    content.replaceChildren(cover(data, 'detail-cover'), el('p', data.publisher || '출판사 정보 없음', 'eyebrow'), el('h2', data.title), el('p', data.authors.join(' · ') || '저자 정보 없음', 'meta'), el('p', `ISBN ${data.isbn || '없음'}`, 'detail-isbn'), el('p', data.description || '제공된 도서 소개가 없습니다.', 'detail-description'), actions);
  } catch (error) { if (request === detailRequest) $('detail-content').replaceChildren(el('h2', '상세 정보를 불러오지 못했어요'), el('p', error.message)); }
}
function card(book) {
  const article = el('article', undefined, 'card'); const picture = el('button', undefined, 'cover-button'); picture.setAttribute('aria-label', `${book.title} 상세 보기`); picture.append(cover(book)); picture.addEventListener('click', () => showDetail(book));
  const body = el('div', undefined, 'card-body'); const title = el('button', book.title, 'book-title'); title.addEventListener('click', () => showDetail(book)); body.append(title, el('p', [...book.authors, book.publisher].filter(Boolean).join(' · ') || '저자 정보 없음', 'meta'));
  if (state.view === 'favorites') { const remove = el('button', '서재에서 삭제', 'save'); remove.addEventListener('click', async () => {
    if (!confirm(`공용 서재에서 “${book.title}”을 삭제할까요?`)) return;
    remove.disabled = true; remove.textContent = '삭제 중…';
    try { await api(`/api/favorites/${book.id}`, { method: 'DELETE' }); saved.delete(book.isbn); if (state.view === 'favorites') await load(); refreshCount(); }
    catch (error) { feedback(error.message, true); remove.disabled = false; remove.textContent = '서재에서 삭제'; }
  }); body.append(remove); } else body.append(saveButton(book));
  article.append(picture, body); return article;
}
async function refreshCount() { try { const data = await api('/api/favorites?page=0&size=1'); $('favorite-count').textContent = data.totalElements.toLocaleString(); } catch { $('favorite-count').textContent = '—'; } }
async function load() {
  const request = ++state.request; const view = state.view; feedback(''); $('results').replaceChildren(); $('results').setAttribute('aria-busy', 'true'); $('empty').hidden = true; $('pagination').hidden = true; $('result-count').textContent = '';
  $('section-title').textContent = view === 'favorites' ? '다시 만나고 싶은 책들' : state.keyword ? `“${state.keyword}” 검색 결과` : '어떤 책이 궁금하세요?';
  if (view === 'search' && !state.keyword) { empty('호기심에서 시작하는 한 권', '검색어를 입력하거나 위의 추천 주제를 선택해보세요.'); $('results').setAttribute('aria-busy', 'false'); return; }
  feedback('불러오는 중…');
  try {
    const data = await api(view === 'search' ? `/api/books/search?keyword=${encodeURIComponent(state.keyword)}&page=${state.searchPage}&size=12` : `/api/favorites?page=${state.favoritePage}&size=12`);
    if (request !== state.request) return;
    const books = view === 'search' ? data.books : data.favorites;
    if (view === 'favorites' && !books.length && state.favoritePage > 0) { state.favoritePage--; return load(); }
    const total = view === 'search' ? data.totalCount : data.totalElements;
    if (view === 'favorites') { $('favorite-count').textContent = total.toLocaleString(); books.forEach(book => saved.add(book.isbn)); }
    $('results').replaceChildren(...books.map(card)); $('result-count').textContent = `${total.toLocaleString()}권`; feedback('');
    if (!books.length) empty(view === 'search' ? '검색 결과가 없어요' : '아직 비어 있는 서재', view === 'search' ? '다른 제목이나 키워드로 검색해보세요.' : '도서 탐색에서 마음에 드는 책을 담아보세요.');
    const page = view === 'search' ? state.searchPage : state.favoritePage + 1;
    state.end = view === 'search' ? data.end || page >= 50 : page >= data.totalPages;
    $('pagination').hidden = !books.length; $('page-label').textContent = `${page} 페이지`; $('previous').disabled = page <= 1; $('next').disabled = state.end;
  } catch (error) { if (request === state.request) { feedback(error.message, true); empty('잠시 연결을 확인해주세요', '문제가 해결되면 검색이나 서재 탭을 눌러 다시 시도할 수 있어요.'); } }
  finally { if (request === state.request) $('results').setAttribute('aria-busy', 'false'); }
}
function changeView(view) { state.view = view; $('search-tools').hidden = view !== 'search'; $('shared-note').hidden = view !== 'favorites'; $('section-kicker').textContent = view === 'search' ? 'FIND YOUR NEXT READ' : 'YOUR SHARED COLLECTION'; for (const name of ['search', 'favorites']) { $(`${name}-tab`).classList.toggle('active', view === name); $(`${name}-tab`).setAttribute('aria-pressed', String(view === name)); } load(); }
$('search-form').addEventListener('submit', (event) => { event.preventDefault(); const keyword = $('keyword').value.trim(); if (!keyword) { $('keyword').focus(); return; } state.keyword = keyword; state.searchPage = 1; load(); });
document.querySelectorAll('[data-keyword]').forEach(button => button.addEventListener('click', () => { $('keyword').value = button.dataset.keyword; $('search-form').requestSubmit(); }));
$('search-tab').addEventListener('click', () => changeView('search')); $('favorites-tab').addEventListener('click', () => changeView('favorites'));
for (const [id, delta] of [['previous', -1], ['next', 1]]) $(id).addEventListener('click', () => { if (state.view === 'search') state.searchPage += delta; else state.favoritePage += delta; load(); });
$('close-detail').addEventListener('click', () => $('detail').close()); $('detail').addEventListener('close', () => { detailRequest++; });
refreshCount();
