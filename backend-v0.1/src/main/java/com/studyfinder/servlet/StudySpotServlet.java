package com.studyfinder.servlet;

import java.io.IOException;
import java.util.List;

import com.studyfinder.dao.StudySpotDAO;
import com.studyfinder.model.StudySpot;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@WebServlet("/study-spots")
public class StudySpotServlet extends HttpServlet {
    


    @Override
    protected void doGet(

            
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        
        StudySpotDAO dao = new StudySpotDAO();
        List<StudySpot> spots = dao.getAllStudySpots();

        request.setAttribute("studySpots", spots);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/study-spots.jsp");


        dispatcher.forward(request, response);
    }
}