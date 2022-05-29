import axios from "../utils/axios";

export const loadMovies = () => axios.get("/movies")