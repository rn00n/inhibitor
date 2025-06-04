import { initTableModule } from '/js/common/table.js';

initTableModule({
    apiUrl: '/backoffice/api/registered-clients',
    searchInputId: 'clientSearchInput',
    tableBodyId: 'clientTableBody',
    paginationId: 'paginationBar',
    renderRowFn: (client) => `
        <td><input class="form-check-input row-checkbox" type="checkbox"></td>
        <td>${client.id}</td>
        <td>${client.clientId}</td>
    `
});