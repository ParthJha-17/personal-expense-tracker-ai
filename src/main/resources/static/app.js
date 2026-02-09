const expenseForm = document.getElementById("expenseForm");
const expenseStatus = document.getElementById("expenseStatus");
const expenseId = document.getElementById("expenseId");
const expenseCancel = document.getElementById("expenseCancel");
const expensesBody = document.getElementById("expensesBody");
const refreshExpensesButton = document.getElementById("refreshExpenses");

const recommendationForm = document.getElementById("recommendationForm");
const recommendationStatus = document.getElementById("recommendationStatus");
const recommendationResult = document.getElementById("recommendationResult");
const recModel = document.getElementById("recModel");
const recRationale = document.getElementById("recRationale");
const recList = document.getElementById("recList");

const api = {
  expenses: "/expenses",
  recommendations: "/recommendations",
};

const formatCurrency = (value) =>
  value === null || value === undefined
    ? "-"
    : `$${Number(value).toFixed(2)}`;

const formatDate = (value) => (value ? value : "-");

const setStatus = (element, message, tone = "info") => {
  element.textContent = message;
  element.dataset.tone = tone;
};

const resetExpenseForm = () => {
  expenseForm.reset();
  expenseId.value = "";
  expenseCancel.classList.add("hidden");
};

const loadExpenses = async () => {
  try {
    const response = await fetch(api.expenses);
    if (!response.ok) {
      throw new Error("Unable to fetch expenses.");
    }
    const data = await response.json();
    renderExpenses(data);
    setStatus(expenseStatus, `Loaded ${data.length} expenses.`);
  } catch (error) {
    setStatus(expenseStatus, error.message, "error");
  }
};

const renderExpenses = (expenses) => {
  expensesBody.innerHTML = "";

  if (!expenses.length) {
    const row = document.createElement("tr");
    const cell = document.createElement("td");
    cell.colSpan = 7;
    cell.textContent = "No expenses yet. Add one above.";
    row.appendChild(cell);
    expensesBody.appendChild(row);
    return;
  }

  expenses.forEach((expense) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${expense.id ?? "-"}</td>
      <td>${expense.description ?? "-"}</td>
      <td>${formatCurrency(expense.amount)}</td>
      <td>${formatDate(expense.date)}</td>
      <td>${expense.category ?? "-"}</td>
      <td>${expense.categoryConfidence ?? "-"}</td>
      <td>
        <div class="actions">
          <button class="action-button edit" data-id="${expense.id}">Edit</button>
          <button class="action-button delete" data-id="${expense.id}">Delete</button>
        </div>
      </td>
    `;
    expensesBody.appendChild(row);
  });
};

const getExpensePayload = () => ({
  amount: Number(document.getElementById("amount").value),
  description: document.getElementById("description").value,
  date: document.getElementById("date").value || null,
});

expenseForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const payload = getExpensePayload();
  const id = expenseId.value;

  try {
    const response = await fetch(id ? `${api.expenses}/${id}` : api.expenses, {
      method: id ? "PUT" : "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });
    if (!response.ok) {
      throw new Error("Unable to save expense.");
    }
    const action = id ? "Updated" : "Added";
    setStatus(expenseStatus, `${action} expense successfully.`);
    resetExpenseForm();
    await loadExpenses();
  } catch (error) {
    setStatus(expenseStatus, error.message, "error");
  }
});

expenseCancel.addEventListener("click", () => {
  resetExpenseForm();
  setStatus(expenseStatus, "Update cancelled.");
});

expensesBody.addEventListener("click", async (event) => {
  const target = event.target;
  if (!(target instanceof HTMLButtonElement)) return;

  const id = target.dataset.id;
  if (!id) return;

  if (target.classList.contains("edit")) {
    try {
      const response = await fetch(`${api.expenses}/${id}`);
      if (!response.ok) {
        throw new Error("Unable to fetch expense.");
      }
      const expense = await response.json();
      document.getElementById("amount").value = expense.amount ?? "";
      document.getElementById("description").value = expense.description ?? "";
      document.getElementById("date").value = expense.date ?? "";
      expenseId.value = expense.id;
      expenseCancel.classList.remove("hidden");
      setStatus(expenseStatus, `Editing expense #${expense.id}.`);
    } catch (error) {
      setStatus(expenseStatus, error.message, "error");
    }
  }

  if (target.classList.contains("delete")) {
    try {
      const response = await fetch(`${api.expenses}/${id}`, {
        method: "DELETE",
      });
      if (!response.ok) {
        throw new Error("Unable to delete expense.");
      }
      setStatus(expenseStatus, `Deleted expense #${id}.`);
      await loadExpenses();
    } catch (error) {
      setStatus(expenseStatus, error.message, "error");
    }
  }
});

refreshExpensesButton.addEventListener("click", loadExpenses);

recommendationForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const payload = {
    goals: document.getElementById("goals").value,
    profile: document.getElementById("profile").value,
  };

  try {
    const response = await fetch(api.recommendations, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });
    if (!response.ok) {
      throw new Error("Unable to fetch recommendations.");
    }
    const data = await response.json();
    recModel.textContent = data.model || "Recommendations";
    recRationale.textContent = data.rationale || "";
    recList.innerHTML = "";
    (data.recommendations || []).forEach((item) => {
      const li = document.createElement("li");
      li.textContent = item;
      recList.appendChild(li);
    });
    recommendationResult.classList.remove("hidden");
    setStatus(recommendationStatus, "Recommendations updated.");
  } catch (error) {
    recommendationResult.classList.add("hidden");
    setStatus(recommendationStatus, error.message, "error");
  }
});

loadExpenses();
