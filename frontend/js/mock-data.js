
const DEMO_MODE = new URLSearchParams(window.location.search).get("demo") === "1";

const MOCK_FACULTIES = [
  { id: 1, name: "Faculty of Informatics" },
  { id: 2, name: "Faculty of Engineering" },
  { id: 3, name: "Faculty of Economics" }
];

const MOCK_CATEGORIES = [
  { id: 1, name: "Electronics" },
  { id: 2, name: "Books" },
  { id: 3, name: "Clothing" },
  { id: 4, name: "Furniture" }
];

const MOCK_LISTINGS = [
  {
    id: 1,
    title: "MacBook Air",
    description: "Used laptop in excellent condition",
    price: 450,
    categoryName: "Electronics",
    facultyId: 1,
    facultyName: "Faculty of Informatics",
    itemCondition: "GOOD",
    createdAt: "2026-11-15T10:00:00",
    imageUrls: []
  },
  {
    id: 2,
    title: "Calculus Textbook",
    description: "University mathematics textbook",
    price: 20,
    categoryName: "Books",
    facultyId: 2,
    facultyName: "Faculty of Engineering",
    itemCondition: "USED",
    createdAt: "2026-11-12T10:00:00",
    imageUrls: []
  },
  {
    id: 3,
    title: "Nike Hoodie",
    description: "Black hoodie size medium",
    price: 35,
    categoryName: "Clothing",
    facultyId: 3,
    facultyName: "Faculty of Economics",
    itemCondition: "NEW",
    createdAt: "2026-11-16T10:00:00",
    imageUrls: []
  },
  {
    id: 4,
    title: "Samsung Monitor",
    description: "24 inch monitor",
    price: 100,
    categoryName: "Electronics",
    facultyId: 2,
    facultyName: "Faculty of Engineering",
    itemCondition: "GOOD",
    createdAt: "2026-11-10T10:00:00",
    imageUrls: []
  },
  {
    id: 5,
    title: "Study Desk",
    description: "Wooden desk for student apartment",
    price: 60,
    categoryName: "Furniture",
    facultyId: 1,
    facultyName: "Faculty of Informatics",
    itemCondition: "USED",
    createdAt: "2026-11-14T10:00:00",
    imageUrls: []
  },
  {
    id: 6,
    title: "Programming in Python",
    description: "Beginner programming book",
    price: 15,
    categoryName: "Books",
    facultyId: 1,
    facultyName: "Faculty of Informatics",
    itemCondition: "NEW",
    createdAt: "2026-11-17T10:00:00",
    imageUrls: []
  }
];

async function mockApiFetch(path) {
  if (path === "/faculties") return MOCK_FACULTIES;
  if (path === "/categories") return MOCK_CATEGORIES;
  if (path === "/listings") return MOCK_LISTINGS;

  throw new Error("Unknown demo API endpoint: " + path);
}
