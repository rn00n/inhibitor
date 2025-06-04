export function initTableModule(config) {
    const {
        apiUrl,
        searchInputId,
        tableBodyId,
        paginationId,
        renderRowFn,
        pageSize = 10
    } = config;

    let currentPage = 1;
    let currentKeyword = '';

    async function search(keyword, page = 1) {
        const response = await doFetch('GET', {
            url: apiUrl,
            query: { keyword, page: page - 1 }
        });

        if (!response.ok) return { content: [], totalPages: 1 };
        return await response.json();
    }

    async function loadData(page = 1, keyword = '') {
        const result = await search(keyword, page);
        renderTable(result.content || []);
        renderPagination(result.totalPages || 1, page);
        updateTotalElementsLabel(result.totalElements || 0);
        currentPage = page;
        currentKeyword = keyword;
    }

    function updateTotalElementsLabel(total) {
        const label = document.getElementById('totalElementsLabel');
        if (label) label.textContent = `총 ${total.toLocaleString()}건`;
    }

    function renderTable(data) {
        const tableBody = document.getElementById(tableBodyId);
        const thead = document.querySelector('thead');

        tableBody.innerHTML = '';
        thead.innerHTML = '';

        if (data.length === 0) return;

        renderTableHeader(Object.keys(data[0]));

        data.forEach(item => {
            const tr = document.createElement('tr');
            tr.classList.add('table-row');

            const checkboxCell = `<td><input class="form-check-input row-checkbox" type="checkbox"></td>`;
            const valueCells = Object.values(item).map(v => `<td>${v}</td>`).join('');

            tr.innerHTML = checkboxCell + valueCells;
            tableBody.appendChild(tr);
        });
        initCheckboxBehavior();
    }

    function renderTableHeader(keys) {
        const thead = document.querySelector('thead');
        const tr = document.createElement('tr');

        const checkboxTh = `<th style="width: 40px;"><input class="form-check-input" type="checkbox" id="checkAll"></th>`;
        const headerThs = keys.map(k => `<th>${k}</th>`).join('');

        tr.innerHTML = checkboxTh + headerThs;
        thead.appendChild(tr);
    }

    function renderPagination(totalPages, currentPage) {
        const pagination = document.getElementById(paginationId);
        pagination.innerHTML = '';
        const maxPages = 5;
        let start = Math.max(currentPage - 2, 1);
        let end = Math.min(start + maxPages - 1, totalPages);
        if (end - start < maxPages - 1) start = Math.max(end - maxPages + 1, 1);

        const addPage = (label, page, disabled = false, isActive = false) => {
            const li = document.createElement('li');
            li.classList.add('page-item');
            if (disabled) li.classList.add('disabled');

            const a = document.createElement('a');
            a.classList.add('page-link', 'border-0');
            if (isActive) a.classList.add('bg-secondary', 'text-white');
            else a.classList.add('bg-light', 'text-muted');
            a.href = '#';
            a.textContent = label;

            if (!disabled) a.addEventListener('click', e => {
                e.preventDefault();
                loadData(page, currentKeyword);
            });

            li.appendChild(a);
            pagination.appendChild(li);
        };

        addPage('<<', 1, currentPage === 1);
        addPage('<', currentPage - 1, currentPage === 1);
        for (let i = start; i <= end; i++) addPage(i, i, false, i === currentPage);
        addPage('>', currentPage + 1, currentPage === totalPages);
        addPage('>>', totalPages, currentPage === totalPages);
    }

    function initCheckboxBehavior() {
        document.querySelectorAll('.table-row').forEach(row => {
            row.addEventListener('click', e => {
                if (e.target.tagName.toLowerCase() !== 'input') {
                    const checkbox = row.querySelector('.row-checkbox');
                    if (checkbox) checkbox.checked = !checkbox.checked;
                }
            });
        });

        const checkAll = document.getElementById('checkAll');
        if (checkAll) {
            checkAll.addEventListener('change', () => {
                const all = document.querySelectorAll('.row-checkbox');
                all.forEach(cb => cb.checked = checkAll.checked);
            });
        }
    }

    function handleSearchInput() {
        const input = document.getElementById(searchInputId);
        input?.addEventListener('input', () => {
            const keyword = input.value.trim();
            loadData(1, keyword);
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        loadData();
        handleSearchInput();
    });
}
