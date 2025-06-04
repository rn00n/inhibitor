import {initTableModule} from '/js/common/table.js';

initTableModule({
    apiUrl: '/backoffice/api/accounts',
    searchInputId: 'accountSearchInput',
    tableBodyId: 'accountTableBody',
    paginationId: 'paginationBar',
    renderRowFn: (account) => `
        <td><input class="form-check-input row-checkbox" type="checkbox"></td>
        <td>${account.id}</td>
        <td>${account.username}</td>
    `
});